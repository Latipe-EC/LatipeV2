package latipe.product.services.product;

import feign.Feign;
import feign.Logger;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.okhttp.OkHttpClient;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import latipe.product.FeignClient.StoreClient;
import latipe.product.configs.CustomAggregationOperation;
import latipe.product.constants.Action;
import latipe.product.entity.Category;
import latipe.product.entity.Product;
import latipe.product.entity.ProductClassification;
import latipe.product.exceptions.BadRequestException;
import latipe.product.exceptions.NotFoundException;
import latipe.product.mapper.ProductMapper;
import latipe.product.producer.RabbitMQProducer;
import latipe.product.repositories.ICategoryRepository;
import latipe.product.repositories.IProductRepository;
import latipe.product.request.BanProductRequest;
import latipe.product.request.CreateProductRequest;
import latipe.product.request.OrderProductCheckRequest;
import latipe.product.request.ProductFeatureRequest;
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
import lombok.AllArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
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
      if (input.productVariants().isEmpty()) {
        if (input.price() == null || input.price() <= 0) {
          throw new BadRequestException("Price must be greater than 0");
        }
        if (input.quantity() <= 0) {
          throw new BadRequestException("Quantity must be greater than 0");
        }
        if (input.images().isEmpty()) {
          throw new BadRequestException("Product must have at least 1 image");
        }
        input.productClassifications().add(ProductClassificationVm.builder()
            .name("Default")
            .price(input.price())
            .quantity(input.quantity())
            .image(input.images().get(0))
            .build());
      } else {
        if (input.productVariants().size() > 2) {
          throw new BadRequestException("Product have maximum 2 variants");
        }
        if (input.productVariants().size() == 1) {
          if (input.productVariants().get(0).options().size() != input.productClassifications()
              .size()) {
            throw new BadRequestException("Product classification must be filled");
          }
          for (int i = 0; i < input.productVariants().get(0).options().size(); i++) {
            input.productClassifications().set(i, ProductClassificationVm.setCodeName(
                input.productClassifications().get(i), String.valueOf(i),
                input.productVariants().get(0).options().get(i)));
          }
        } else {
          int count =
              input.productVariants().get(0).options().size() * input.productVariants().get(1)
                  .options().size();
          if (count != input.productClassifications().size()) {
            throw new BadRequestException("Product classification must be filled");
          }
          count = 0;
          for (int i = 0; i < input.productVariants().get(0).options().size(); i++) {
            for (int j = 0; j < input.productVariants().get(1).options().size(); j++) {
              input.productClassifications().set(i, ProductClassificationVm.setCodeName(
                  input.productClassifications().get(i), String.valueOf(i),
                  input.productVariants().get(0).options().get(i) + " - " + input.productVariants()
                      .get(1).options().get(j)));
              count++;
            }
          }
        }

      }
      var prod = productMapper.mapToProductBeforeCreate(input);
      // get store id from store service
      StoreClient storeClient = Feign.builder()
          .client(new OkHttpClient())
          .encoder(new GsonEncoder())
          .decoder(new GsonDecoder())
          .logLevel(Logger.Level.FULL)
          .target(StoreClient.class, "http://localhost:8181/api/v1");
      prod.setStoreId(storeClient.getStoreId(request.getHeader("Authorization"), userId));
      var savedProd = productRepository.save(prod);

      // send message create message
      var message = ParseObjectToString.parse(
          new ProductMessageVm(savedProd.getId(), Action.CREATE));
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
          productPriceVm = ProductPriceVm.builder()
              .code(code)
              .image(classification.getImage())
              .price(classification.getPrice())
              .quantity(classification.getQuantity())
              .build();
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
//            String query =
//                    """
//                                {
//                                    "$unwind": "$productClassifications"
//                                }
//                            """;
//            TypedAggregation<ProductClassification> test = Aggregation.newAggregation(
//                    ProductClassification.class,
//                    Aggregation.match(Criteria.where("_id").in(prodOrders.stream().map(OrderProductCheckRequest::productId).toList())),
//                    // unwind product variant
//                    new CustomAggregationOperation(query),
//                    Aggregation.match(Criteria.where("productClassifications._id").in(prodOrders.stream().map(OrderProductCheckRequest::optionId).toList()))
//            );
      var aggregate = createQueryClassification(
          prodOrders.stream().map(OrderProductCheckRequest::productId).toList(),
          prodOrders.stream().map(OrderProductCheckRequest::optionId).toList());
      AggregationResults<Document> results = mongoTemplate.aggregate(aggregate,
          ProductClassification.class, Document.class);
      List<Document> documents = results.getMappedResults();
      List<ProductOrderVm> orders = documents.stream()
          .map(doc -> {
            Document productClassificationsDoc = doc.get("productClassifications", Document.class);
            OrderProductCheckRequest prodOrder = prodOrders.stream().filter(
                    x -> x.productId()
                        .equals(doc.getString("_id")) && x.optionId()
                        .equals(productClassificationsDoc.getString("_id")))
                .findFirst().orElseThrow(() -> new BadRequestException("Product not found"));
            if (productClassificationsDoc.getInteger("quantity") < prodOrder.quantity()) {
              throw new BadRequestException("Product out of stock");
            }
            return ProductOrderVm.builder()
                .productId(doc.getString("_id"))
                .optionId(productClassificationsDoc.getString("_id"))
                .quantity(prodOrder.quantity())
                .price(productClassificationsDoc.getDouble("price"))
                .nameOption(productClassificationsDoc.getString("name"))
                .totalPrice(productClassificationsDoc.getDouble("price") * prodOrder.quantity())
                .build();
          }).toList();
      return OrderProductResponse.builder()
          .totalPrice(orders.stream().mapToDouble(ProductOrderVm::totalPrice).sum())
          .products(orders)
          .build();

//            List<Product> products = productRepository.findByIds(prodOrders.stream().map(OrderProductCheckRequest::productId).toList());
//            if (products.size() != prodOrders.size()) {
//                throw new BadRequestException("Product not found");
//            }
//            List<ProductOrderVm> orders = new ArrayList<>();
//            for (OrderProductCheckRequest prodOrder : prodOrders) {
//                ProductClassification prodVariant = products.stream().flatMap(product -> product.getProductClassifications().stream())
//                        .filter(classification -> classification.getId().equals(prodOrder.optionId()))
//                        .findFirst()
//                        .orElseThrow(() -> new BadRequestException("Product classification not found"));
//                if (prodVariant.getQuantity() < prodOrder.quantity()) {
//                    throw new BadRequestException("Product out of stock");
//                }
//                orders.add(ProductOrderVm.builder()
//                        .productId(prodOrder.productId())
//                        .optionId(prodOrder.optionId())
//                        .quantity(prodOrder.quantity())
//                        .price(prodVariant.getPrice())
//                        .nameOption(prodVariant.getName())
//                        .build());
//                total += prodVariant.getPrice() * prodOrder.quantity();
//            }
//            return OrderProductResultsDto.builder()
//                    .totalPrice(total)
//                    .products(orders)
//                    .build();
    });
  }

  @Override
  @Async
  public CompletableFuture<ProductESDetailVm> getProductESDetailById(String productId) {
    return CompletableFuture.supplyAsync(
        () -> {
          Product product = productRepository
              .findById(productId)
              .orElseThrow(() ->
                  new NotFoundException("PRODUCT_NOT_FOUND", productId)
              );

          List<String> categoryNames = categoryRepository.findAllById(product.getCategories())
              .stream().map(Category::getName).toList();
          return new ProductESDetailVm(
              product.getId(),
              product.getName(),
              product.getSlug(),
              product.getPrice(),
              product.isPublished(),
              product.getImages(),
              product.getDescription(),
              product.getProductClassifications(),
              product.getProductClassifications().stream().map(ProductClassification::getName)
                  .toList(),
              categoryNames,
              product.isBanned(),
              product.isDeleted(),
              product.getCreatedDate()
          );
        }
    );
  }

  @Override
  @Async
  public CompletableFuture<List<ProductThumbnailVm>> getFeatureProduct(
      List<ProductFeatureRequest> products) {
    return CompletableFuture.supplyAsync(() -> {
      var aggregate = createQueryClassification(
          products.stream().map(ProductFeatureRequest::productId).toList(), products.stream().map(
              ProductFeatureRequest::optionId).toList());
      AggregationResults<Document> results = mongoTemplate.aggregate(aggregate,
          ProductClassification.class, Document.class);
      List<Document> documents = results.getMappedResults();
      return documents.stream()
          .map(doc -> {
            Document productClassificationsDoc = doc.get("productClassifications", Document.class);
            return new ProductThumbnailVm(doc.getString("_id"),
                doc.getString("name"),
                productClassificationsDoc.getString("name"),
                productClassificationsDoc.getDouble("price"),
                productClassificationsDoc.getString("image"));
          }).toList();
    });
  }

  @Override
  @Async
  public CompletableFuture<ProductResponse> update(String userId, String id,
      UpdateProductRequest input, HttpServletRequest request) {
    return null;
//    Product product = productRepository.findById(id)
//        .orElseThrow(() -> new BadRequestException("Product not found"));
//    // check permission to change product (store service)
//    StoreClient storeClient = Feign.builder()
//        .client(new OkHttpClient())
//        .encoder(new GsonEncoder())
//        .decoder(new GsonDecoder())
//        .logLevel(Logger.Level.FULL)
//        .target(StoreClient.class, "http://localhost:8181/api/v1");
//    var store = storeClient.getStoreId(request.getHeader("Authorization"), userId);
//    if (!store.equals(product.getStoreId())) {
//      throw new BadRequestException("You don't have permission to change this product");
//    }
//    if (input.productVariants().size() == 0) {
//      if (input.price() == null || input.price() <= 0) {
//        throw new BadRequestException("Price must be greater than 0");
//      }
//      if (input.quantity() <= 0) {
//        throw new BadRequestException("Quantity must be greater than 0");
//      }
//      if (input.images().size() == 0) {
//        throw new BadRequestException("Product must have at least 1 image");
//      }
//      input.productClassifications().add(ProductClassificationVm.builder()
//          .name("Default")
//          .price(input.price())
//          .quantity(input.quantity())
//          .image(input.images().get(0))
//          .build());
//    } else {
//      if (input.productVariants().size() > 2) {
//        throw new BadRequestException("Product have maximum 2 variants");
//      }
//      if (input.productVariants().size() == 1) {
//        if (input.productVariants().get(0).options().size() != input.productClassifications()
//            .size()) {
//          throw new BadRequestException("Product classification must be filled");
//        }
//        for (int i = 0; i < input.productVariants().get(0).options().size(); i++) {
//          input.productClassifications().get(i).code(String.valueOf(i));
//        }
//      } else {
//        int count =
//            input.productVariants().get(0).options().size() * input.productVariants().get(1)
//                .options().size();
//        if (count != input.productClassifications().size()) {
//          throw new BadRequestException("Product classification must be filled");
//        }
//        count = 0;
//        for (int i = 0; i < input.productVariants().get(0).options().size(); i++) {
//          for (int j = 0; j < input.productVariants().get(1).options().size(); j++) {
//            input.productClassifications().get(count)
//                .code(String.valueOf(i) + String.valueOf(j));
//            count++;
//          }
//        }
//      }
//    }
//    var savedProd = productRepository.save(product);
//
//    // send message create message
//    var message = ParseObjectToString.parse(new ProductMessageVm(savedProd.getId(), Action.UPDATE));
//    rabbitMQProducer.sendMessage(message);
//
//    return CompletableFuture.completedFuture(productMapper.mapToProductToResponse(savedProd));
  }

  @Override
  @Async
  public CompletableFuture<Void> remove(String userId, String id, HttpServletRequest request) {
    return CompletableFuture.supplyAsync(() -> {
      Product product = productRepository.findById(id)
          .orElseThrow(() -> new BadRequestException("Product not found"));
      // check permission to change product (store service)
      StoreClient storeClient = Feign.builder()
          .client(new OkHttpClient())
          .encoder(new GsonEncoder())
          .decoder(new GsonDecoder())
          .logLevel(Logger.Level.FULL)
          .target(StoreClient.class, "http://localhost:8181/api/v1");
      var store = storeClient.getStoreId(request.getHeader("Authorization"), userId);
      if (!store.equals(product.getStoreId())) {
        throw new BadRequestException("You don't have permission to change this product");
      }
      product.setDeleted(true);
      var savedProduct = productRepository.save(product);

      // send message create message
      var message = ParseObjectToString.parse(
          new ProductMessageVm(savedProduct.getId(), Action.DELETE));
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
      var message = ParseObjectToString.parse(
          new ProductMessageVm(savedProduct.getId(), Action.BAN));
      rabbitMQProducer.sendMessage(message);

      return null;
    });
  }

  private TypedAggregation<ProductClassification> createQueryClassification(List<String> prods,
      List<String> options) {
    String query =
        """
                {
                    "$unwind": "$productClassifications"
                }
            """;
    return Aggregation.newAggregation(
        ProductClassification.class,
        Aggregation.match(Criteria.where("_id").in(prods)),
        new CustomAggregationOperation(query),
        Aggregation.match(Criteria.where("productClassifications._id").in(options))
    );
  }
}
