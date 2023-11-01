package latipe.product.services.product;

import static latipe.product.configs.test.generateHash;
import static latipe.product.configs.test.getPrivateKey;
import static latipe.product.constants.CONSTANTS.URL;

import com.fasterxml.jackson.core.JsonProcessingException;
import feign.Feign;
import feign.Logger;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.okhttp.OkHttpClient;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import latipe.product.FeignClient.StoreClient;
import latipe.product.configs.CustomAggregationOperation;
import latipe.product.constants.Action;
import latipe.product.entity.Category;
import latipe.product.entity.Product;
import latipe.product.entity.product.ProductClassification;
import latipe.product.exceptions.BadRequestException;
import latipe.product.exceptions.NotFoundException;
import latipe.product.mapper.ProductMapper;
import latipe.product.producer.RabbitMQProducer;
import latipe.product.repositories.ICategoryRepository;
import latipe.product.repositories.IProductRepository;
import latipe.product.request.BanProductRequest;
import latipe.product.request.CreateProductRequest;
import latipe.product.request.GetProvinceCodesRequest;
import latipe.product.request.OrderProductCheckRequest;
import latipe.product.request.ProductFeatureRequest;
import latipe.product.request.UpdateProductQuantityRequest;
import latipe.product.request.UpdateProductRequest;
import latipe.product.response.OrderProductResponse;
import latipe.product.response.ProductResponse;
import latipe.product.utils.ParseObjectToString;
import latipe.product.viewmodel.ProductClassificationVm;
import latipe.product.viewmodel.ProductESDetailVm;
import latipe.product.viewmodel.ProductMessageVm;
import latipe.product.viewmodel.ProductOrderVm;
import latipe.product.viewmodel.ProductPriceVm;
import latipe.product.viewmodel.ProductThumbnailVm;
import latipe.product.viewmodel.ProductVariantVm;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {

  private final IProductRepository productRepository;
  private final ICategoryRepository categoryRepository;
  private final ProductMapper productMapper;
  private final MongoTemplate mongoTemplate;
  private final RabbitMQProducer rabbitMQProducer;


  @Override
  @Async
  public CompletableFuture<ProductResponse> create(String userId, CreateProductRequest input,
      HttpServletRequest request) {
    return CompletableFuture.supplyAsync(() -> {
      if (input.images().isEmpty()) {
        throw new BadRequestException("Product must have at least 1 image");
      }
      if (input.productVariants().isEmpty()) {
        checkProductNoOption(input.price(), input.quantity());

        input.productClassifications().add(
            ProductClassificationVm.builder().name("Default").price(input.price())
                .promotionalPrice(input.promotionalPrice()).quantity(input.quantity())
                .promotionalPrice(input.promotionalPrice()).image(input.images().get(0)).build());
      } else {
        CheckProductHaveOption(input.productVariants(), input.productClassifications());
      }
      StoreClient storeClient = Feign.builder().client(new OkHttpClient())
          .encoder(new GsonEncoder()).decoder(new GsonDecoder()).logLevel(Logger.Level.FULL)
          .target(StoreClient.class, "http://localhost:8181/api/v1");
      // get store id from store service
      var storeId = storeClient.getStoreId(request.getHeader("Authorization"), userId);

      var prod = productMapper.mapToProductBeforeCreate(input, storeId);
      var savedProd = productRepository.save(prod);

      // send message create message
      String message;
      try {
        message = ParseObjectToString.parse(new ProductMessageVm(savedProd.getId(), Action.CREATE));
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
      rabbitMQProducer.sendMessage(message);

      return productMapper.mapToProductToResponse(savedProd);
    });

  }

  @Override
  @Async
  public CompletableFuture<ProductPriceVm> getPrice(String prodId, String code) {
    return CompletableFuture.supplyAsync(() -> {
      Product product = productRepository.findById(prodId)
          .orElseThrow(() -> new BadRequestException("Product not found"));
      if (product.getPrice() > 0) {
        throw new BadRequestException("Product is not have variant");
      }
      for (ProductClassification classification : product.getProductClassifications()) {
        if (classification.getCode().equals(code)) {
          ProductPriceVm productPriceVm;
          productPriceVm = ProductPriceVm.builder().code(code).image(classification.getImage())
              .price(classification.getPrice()).quantity(classification.getQuantity()).build();
          return productPriceVm;
        }
      }
      throw new NotFoundException("Product classification not found");
    });
  }

  @Override
  @Async
  public CompletableFuture<OrderProductResponse> checkProductInStock(
      List<OrderProductCheckRequest> prodOrders) {
    return CompletableFuture.supplyAsync(() -> {

      // handle case product id and option id is same
      Set<OrderProductCheckRequest> orderProductSet = new HashSet<>();
      for (OrderProductCheckRequest orderProduct : prodOrders) {
        OrderProductCheckRequest existingProduct = orderProductSet.stream()
            .filter(p -> p.equals(orderProduct)).findFirst().orElse(null);
        if (existingProduct != null) {
          orderProductSet.remove(existingProduct);
          orderProductSet.add(existingProduct.merge(orderProduct));
        } else {
          orderProductSet.add(orderProduct);
        }
      }

      var prodFilter = orderProductSet.stream().map(OrderProductCheckRequest::productId).toList();
      var optionFilter = orderProductSet.stream()
          .map(x -> "ObjectId(\"%s\")".formatted(x.optionId())).toList();

      var aggregate = createQueryClassification(prodFilter, optionFilter);

      var results = mongoTemplate.aggregate(aggregate, Product.class, Document.class);
      var documents = results.getMappedResults();
      List<String> storeIds = new ArrayList<>();

      var orders = documents.stream().map(doc -> {
        Document productClassificationsDoc = doc.get("productClassifications", Document.class);

        OrderProductCheckRequest prodOrder = orderProductSet.stream().filter(
                x -> x.productId().equals(doc.getObjectId("_id").toString()) && x.optionId()
                    .equals(productClassificationsDoc.getObjectId("_id").toString())).findFirst()
            .orElseThrow(() -> new BadRequestException("Product not found"));
        if (productClassificationsDoc.getInteger("quantity") < prodOrder.quantity()) {
          throw new BadRequestException("Product out of stock");
        }

        Double promotionalPrice = 0.0;
        if (productClassificationsDoc.get("promotionalPrice") != null) {
          promotionalPrice = productClassificationsDoc.getDouble("promotionalPrice");
        }

        storeIds.add(doc.getString("storeId"));

        return ProductOrderVm.builder().productId(doc.getObjectId("_id").toString())
            .name(doc.getString("name"))
            .optionId(productClassificationsDoc.getObjectId("_id").toString())
            .quantity(prodOrder.quantity()).price(productClassificationsDoc.getDouble("price"))
            .promotionalPrice(promotionalPrice)
            .nameOption(productClassificationsDoc.getString("name")).totalPrice(
                productClassificationsDoc.getDouble("promotionalPrice") == null ?
                    productClassificationsDoc.getDouble("price") * prodOrder.quantity()
                    : productClassificationsDoc.getDouble("promotionalPrice")
                        * prodOrder.quantity()).storeId(doc.getString("storeId")).build();
      }).toList();

      if (orders.size() != orderProductSet.size()) {
        throw new NotFoundException("Product not found");
      }
      var storeClient = Feign.builder().client(new OkHttpClient()).encoder(new GsonEncoder())
          .decoder(new GsonDecoder()).logLevel(Logger.Level.FULL).target(StoreClient.class, URL);

      String hash = null;
      try {
        hash = generateHash("cart-service", getPrivateKey());
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      var storeProvinceCodes = storeClient.getProvinceCodes(hash,
          GetProvinceCodesRequest.builder().ids(storeIds).build());

      return OrderProductResponse.builder()
          .totalPrice(orders.stream().mapToDouble(ProductOrderVm::totalPrice).sum())
          .products(orders).storeProvinceCodes(storeProvinceCodes.codes()).build();
    });
  }

  @Override
  @Async
  public CompletableFuture<ProductESDetailVm> getProductESDetailById(String productId) {
    return CompletableFuture.supplyAsync(() -> {
      Product product = productRepository.findById(productId)
          .orElseThrow(() -> new NotFoundException("PRODUCT_NOT_FOUND", productId));

      List<String> categoryNames = categoryRepository.findAllById(product.getCategories()).stream()
          .map(Category::getName).toList();
      return new ProductESDetailVm(product.getId(), product.getName(), product.getSlug(),
          product.getPrice(), product.isPublished(), product.getImages(), product.getDescription(),
          product.getProductClassifications(),
          product.getProductClassifications().stream().map(ProductClassification::getName).toList(),
          categoryNames, product.isBanned(), product.isDeleted(), product.getCreatedDate());
    });
  }

  @Override
  @Async
  public CompletableFuture<List<ProductThumbnailVm>> getFeatureProduct(
      List<ProductFeatureRequest> products) {
    return CompletableFuture.supplyAsync(() -> {

      var prodFilter = products.stream().map(ProductFeatureRequest::productId).toList();
      var optionFilter = products.stream().map(x -> "ObjectId(\"%s\")".formatted(x.optionId()))
          .toList();

      var aggregate = createQueryClassification(prodFilter, optionFilter);
      AggregationResults<Document> results = mongoTemplate.aggregate(aggregate,
          ProductClassification.class, Document.class);
      List<Document> documents = results.getMappedResults();
      return documents.stream().map(doc -> {
        Document productClassificationsDoc = doc.get("productClassifications", Document.class);
        return new ProductThumbnailVm(doc.getObjectId("_id").toString(), doc.getString("name"),
            productClassificationsDoc.getString("name"),
            productClassificationsDoc.getDouble("price"),
            productClassificationsDoc.getString("image"));
      }).toList();
    });
  }

  @Override
  @Async
  @Transactional
  public CompletableFuture<Void> updateQuantity(List<UpdateProductQuantityRequest> request) {
    return CompletableFuture.supplyAsync(() -> {
      List<Product> products = new ArrayList<>();
      for (UpdateProductQuantityRequest req : request) {

        Product product;
        int index = products.indexOf(new Product(req.productId()));
        if (index < 0) {
          product = productRepository.findById(req.productId())
              .orElseThrow(() -> new BadRequestException("Product not found"));
        } else {
          product = products.get(index);
        }

        boolean isFound = false;
        for (ProductClassification productClassification : product.getProductClassifications()) {
          if (productClassification.getId().equals(req.optionId())) {
            if (productClassification.getQuantity() < req.quantity()) {
              throw new BadRequestException("Product out of stock");
            }
            productClassification.setQuantity(productClassification.getQuantity() - req.quantity());
            product.setCountSale(product.getCountSale() + req.quantity());
            isFound = true;
            break;
          }
        }

        if (!isFound) {
          throw new BadRequestException("Product classification not found");
        }

        if (index < 0) {
          products.add(product);
        }
      }

      products = productRepository.saveAll(products);
      for (Product prod : products) {
        String message;
        try {
          message = ParseObjectToString.parse(new ProductMessageVm(prod.getId(), Action.CREATE));
        } catch (JsonProcessingException e) {
          throw new RuntimeException(e);
        }
        rabbitMQProducer.sendMessage(message);
      }

      return null;
    });
  }

  @Override
  @Async
  @Transactional
  public CompletableFuture<ProductResponse> update(String userId, String id,
      UpdateProductRequest input, HttpServletRequest request) {
    var product = productRepository.findById(id)
        .orElseThrow(() -> new BadRequestException("Product not found"));

    // check permission to change product (store service)
    var storeClient = Feign.builder().client(new OkHttpClient()).encoder(new GsonEncoder())
        .decoder(new GsonDecoder()).logLevel(Logger.Level.FULL).target(StoreClient.class, URL);
    var store = storeClient.getStoreId(request.getHeader("Authorization"), userId);

    if (!store.equals(product.getStoreId())) {
      throw new BadRequestException("You don't have permission to change this product");
    }

    if (input.images().isEmpty()) {
      throw new BadRequestException("Product must have at least 1 image");
    }
    if (input.productVariants().isEmpty()) {

      checkProductNoOption(input.price(), input.quantity());

      input.productClassifications().clear();
      input.productClassifications().add(
          ProductClassificationVm.builder().name("Default").price(input.price())
              .quantity(input.quantity()).image(input.images().get(0)).build());
    } else {
      CheckProductHaveOption(input.productVariants(), input.productClassifications());
    }

    var savedProd = productMapper.mapToProductBeforeCreate(product.getId(), input, store);

    // send message create message
    String message;
    try {
      message = ParseObjectToString.parse(new ProductMessageVm(savedProd.getId(), Action.UPDATE));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    rabbitMQProducer.sendMessage(message);

    return CompletableFuture.completedFuture(productMapper.mapToProductToResponse(savedProd));
  }

  private void CheckProductHaveOption(List<ProductVariantVm> productVariantVms,
      List<ProductClassificationVm> productClassificationVms) {
    if (productVariantVms.size() > 2) {
      throw new BadRequestException("Product have maximum 2 variants");
    }

    if (productVariantVms.size() == 1) {
      if (productVariantVms.get(0).options().size() != productClassificationVms.size()) {
        throw new BadRequestException("Product classification must be filled");
      }
      for (int i = 0; i < productVariantVms.get(0).options().size(); i++) {
        productClassificationVms.set(i,
            ProductClassificationVm.setCodeName(productClassificationVms.get(i), String.valueOf(i),
                productVariantVms.get(0).options().get(i)));
      }
    } else {
      int count =
          productVariantVms.get(0).options().size() * productVariantVms.get(1).options().size();

      if (count != productClassificationVms.size()) {
        throw new BadRequestException("Product classification must be filled");
      }

      count = 0;

      for (int i = 0; i < productVariantVms.get(0).options().size(); i++) {
        for (int j = 0; j < productVariantVms.get(1).options().size(); j++) {
          productClassificationVms.set(i,
              ProductClassificationVm.setCodeName(productClassificationVms.get(i),
                  String.valueOf(i),
                  productVariantVms.get(0).options().get(i) + " - " + productVariantVms.get(1)
                      .options().get(j)));

          count++;
        }
      }
    }
  }

  private void checkProductNoOption(Double price, int quantity) {
    if (price == null || price <= 0) {
      throw new BadRequestException("Price must be greater than 0");
    }
    if (quantity <= 0) {
      throw new BadRequestException("Quantity must be greater than 0");
    }


  }

  @Override
  @Async
  public CompletableFuture<Void> remove(String userId, String id, HttpServletRequest request) {
    return CompletableFuture.supplyAsync(() -> {
      Product product = productRepository.findById(id)
          .orElseThrow(() -> new BadRequestException("Product not found"));
      // check permission to change product (store service)
      StoreClient storeClient = Feign.builder().client(new OkHttpClient())
          .encoder(new GsonEncoder()).decoder(new GsonDecoder()).logLevel(Logger.Level.FULL)
          .target(StoreClient.class, "http://localhost:8181/api/v1");
      var store = storeClient.getStoreId(request.getHeader("Authorization"), userId);
      if (!store.equals(product.getStoreId())) {
        throw new BadRequestException("You don't have permission to change this product");
      }
      product.setDeleted(true);
      var savedProduct = productRepository.save(product);

      // send message create message
      String message;
      try {
        message = ParseObjectToString.parse(
            new ProductMessageVm(savedProduct.getId(), Action.UPDATE));
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
      rabbitMQProducer.sendMessage(message);

      return null;
    });
  }

  @Override
  @Async
  public CompletableFuture<Void> ban(String id, BanProductRequest input) {
    return CompletableFuture.supplyAsync(() -> {
      Product product = productRepository.findById(id)
          .orElseThrow(() -> new BadRequestException("Product not found"));
      // check permission to change product (store service)
      product.setReasonBan(input.reason());
      product.setBanned(true);
      var savedProduct = productRepository.save(product);

      // send message create message
      String message;
      try {
        message = ParseObjectToString.parse(new ProductMessageVm(savedProduct.getId(), Action.BAN));
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }

      rabbitMQProducer.sendMessage(message);

      return null;
    });
  }

  private TypedAggregation<ProductClassification> createQueryClassification(List<String> prods,
      List<String> options) {
    String matchProduct = """
        {$match: {
            "_id": {
                $in: [%s]
            }
          }
        }""";
    String unwind = """
        { $unwind: "$productClassifications"
        }
        """;
    String matchOption = """
        {$match: {
            "productClassifications._id": {
                $in: [%s]
            }
          }
        }""";

    return Aggregation.newAggregation(ProductClassification.class,
        Aggregation.match(Criteria.where("_id").in(prods)), new CustomAggregationOperation(unwind),
        new CustomAggregationOperation(matchOption.formatted(String.join(",", options))));
  }

}
