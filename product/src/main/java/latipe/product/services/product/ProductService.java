package latipe.product.services.product;

import static latipe.product.utils.GenTokenInternal.generateHash;
import static latipe.product.utils.GenTokenInternal.getPrivateKey;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import latipe.product.configs.CustomAggregationOperation;
import latipe.product.configs.SecureInternalProperties;
import latipe.product.constants.Action;
import latipe.product.constants.EStatusBan;
import latipe.product.dtos.PagedResultDto;
import latipe.product.dtos.Pagination;
import latipe.product.entity.Category;
import latipe.product.entity.Product;
import latipe.product.entity.product.ProductClassification;
import latipe.product.exceptions.BadRequestException;
import latipe.product.exceptions.NotFoundException;
import latipe.product.feign.StoreClient;
import latipe.product.mapper.CategoryMapper;
import latipe.product.mapper.ProductMapper;
import latipe.product.producer.RabbitMQProducer;
import latipe.product.repositories.ICategoryRepository;
import latipe.product.repositories.IProductRepository;
import latipe.product.request.BanProductRequest;
import latipe.product.request.CreateProductRequest;
import latipe.product.request.GetProvinceCodesRequest;
import latipe.product.request.MultipleStoreRequest;
import latipe.product.request.OrderProductCheckRequest;
import latipe.product.request.ProductFeatureRequest;
import latipe.product.request.UpdateProductQuantityRequest;
import latipe.product.request.UpdateProductRequest;
import latipe.product.response.OrderProductResponse;
import latipe.product.response.ProductAdminResponse;
import latipe.product.response.ProductDetailResponse;
import latipe.product.response.ProductResponse;
import latipe.product.response.ProductStoreResponse;
import latipe.product.utils.ParseObjectToString;
import latipe.product.viewmodel.ProductClassificationVm;
import latipe.product.viewmodel.ProductESDetailVm;
import latipe.product.viewmodel.ProductMessageVm;
import latipe.product.viewmodel.ProductOrderVm;
import latipe.product.viewmodel.ProductPriceVm;
import latipe.product.viewmodel.ProductSample;
import latipe.product.viewmodel.ProductThumbnailVm;
import latipe.product.viewmodel.ProductVariantVm;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ProductService.class);
  private final IProductRepository productRepository;
  private final ICategoryRepository categoryRepository;
  private final ProductMapper productMapper;
  private final MongoTemplate mongoTemplate;
  private final RabbitMQProducer rabbitMQProducer;
  private final CategoryMapper categoryMapper;
  private final SecureInternalProperties secureInternalProperties;
  private final Gson gson;
  private final StoreClient storeClient;
  @Value("${rabbitmq.exchange.name}")
  private String exchange;
  @Value("${rabbitmq.routing.key}")
  private String routingKey;

  @Async
  @Override
  public CompletableFuture<PagedResultDto<ProductAdminResponse>> getAdminProduct(long skip,
      int limit, String name, String orderBy, EStatusBan ban) {
    return CompletableFuture.supplyAsync(() -> {
      List<Boolean> banCriteria;
      if (ban == EStatusBan.ALL) {
        banCriteria = List.of(true, false);
      } else if (ban == EStatusBan.TRUE) {
        banCriteria = List.of(true);
      } else {
        banCriteria = List.of(false);
      }
      var criteriaSearch = new Criteria();
      criteriaSearch.orOperator(
          Criteria.where("_id").in(name),
          Criteria.where("name").regex(name, "i")
      );

      Direction direction = orderBy.charAt(0) == '-' ? Direction.DESC : Direction.ASC;
      String orderByField = orderBy.charAt(0) == '-' ? orderBy.substring(1) : orderBy;
      var aggregate = Aggregation.newAggregation(ProductStoreResponse.class, Aggregation.match(
              Criteria
                  .where("isBanned").in(banCriteria)
                  .andOperator(criteriaSearch)), Aggregation.skip(skip), Aggregation.limit(limit),
          Aggregation.sort(direction, orderByField));

      var total = productRepository.countAdminProduct(banCriteria, name);
      var results = mongoTemplate.aggregate(aggregate, Product.class, Document.class);
      var documents = results.getMappedResults();
      var list = documents.stream().map(doc -> {
        var prod = gson.fromJson(doc.toJson(), Product.class);
        var price =
            prod.getProductClassifications().get(0).getPromotionalPrice() != null
                && prod.getProductClassifications().get(0).getPromotionalPrice() > 0
                ? prod.getProductClassifications().get(0).getPromotionalPrice()
                : prod.getProductClassifications().get(0).getPrice();

        return ProductAdminResponse.builder().id(doc.getObjectId("_id").toString())
            .name(prod.getName()).image(prod.getImages().get(0))
            .countProductVariants(prod.getProductVariants().size())
            .countSale(doc.getInteger("countSale"))
            .isBanned(doc.getBoolean("isBanned"))
            .reasonBan(doc.getString("reasonBan")).price(price)
            .rating(calculateRatingAverage(prod.getRatings()))
            .isDeleted(doc.getBoolean("isDeleted")).build();
      });
      return PagedResultDto.create(Pagination.create(total, skip, limit), list.toList());
    });
  }

  @Async
  @Override
  public CompletableFuture<PagedResultDto<ProductStoreResponse>> getMyProductStore(long skip,
      int limit, String name, String orderBy, String storeId) {
    return CompletableFuture.supplyAsync(() -> {
      var aggregate = createQueryProduct(skip, limit, name, orderBy, storeId, false, false);
      var total = productRepository.countProductByStoreId(storeId, name);
      return getProductResponsePagedResultDto(skip, limit, total, aggregate);
    });
  }

  @Async
  @Override
  public CompletableFuture<PagedResultDto<ProductStoreResponse>> getBanProductStore(long skip,
      int limit, String name, String orderBy, String storeId) {
    return CompletableFuture.supplyAsync(() -> {
      var aggregate = createQueryProduct(skip, limit, name, orderBy, storeId, true, false);
      var total = productRepository.countProductBanByStoreId(storeId, name);
      return getProductResponsePagedResultDto(skip, limit, total, aggregate);
    });
  }

  @Override
  @Async
  public CompletableFuture<ProductResponse> get(String userId, String prodId,
      HttpServletRequest request) {
    return CompletableFuture.supplyAsync(() -> {
      var prod = productRepository.findById(prodId)
          .orElseThrow(() -> new BadRequestException("Product not found"));
      // get store id from store service
      var storeId = storeClient.getStoreId(request.getHeader("Authorization"), userId);

      if (!storeId.equals(prod.getStoreId())) {
        throw new BadRequestException("You don't have permission to view this product");
      }
      var categories = categoryRepository.findAllById(prod.getCategories());
      return productMapper.mapToProductToResponse(prod, categories);
    });
  }

  @Override
  @Async
  public CompletableFuture<ProductResponse> create(String userId, CreateProductRequest input,
      HttpServletRequest request) {
    return CompletableFuture.supplyAsync(() -> {
      if (input.images().isEmpty()) {
        throw new BadRequestException("Product must have at least 1 image");
      }
      if (input.images().stream().anyMatch(String::isBlank)) {
        throw new BadRequestException("Image is required");
      }

      if (input.productVariants().isEmpty()) {
        checkProductNoOption(input.price());

        input.productClassifications().add(
            ProductClassificationVm.builder().name("Default").price(input.price())
                .promotionalPrice(input.promotionalPrice()).quantity(input.quantity())
                .promotionalPrice(input.promotionalPrice()).build());
      } else {
        CheckProductHaveOption(input.productVariants(), input.productClassifications());
      }
      // get store id from store service
      var storeId = storeClient.getStoreId(request.getHeader("Authorization"), userId);

      var prod = productMapper.mapToProductBeforeCreate(input, storeId);
      prod.setIsPublished(input.isPublished());
      var savedProd = productRepository.save(prod);

      // send message create message
      String message;
      try {
        message = ParseObjectToString.parse(
            new ProductMessageVm(savedProd.getId(), Action.CREATE, null));
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
      rabbitMQProducer.sendMessage(exchange, routingKey, message);

      return productMapper.mapToProductToResponse(savedProd, null);
    });

  }

  @Override
  @Async
  public CompletableFuture<Long> countAllProduct() {
    return CompletableFuture.supplyAsync(productRepository::count);
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
      long startTime = System.nanoTime();
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

        var productClassificationsDoc = doc.get("productClassifications", Document.class);

        OrderProductCheckRequest prodOrder = orderProductSet.stream().filter(
                x -> x.productId().equals(doc.getObjectId("_id").toString()) && x.optionId()
                    .equals(productClassificationsDoc.getObjectId("_id").toString())).findFirst()
            .orElseThrow(() -> new BadRequestException("Product not found"));

        if (productClassificationsDoc.getInteger("quantity") < prodOrder.quantity()) {
          throw new BadRequestException("Product out of stock");
        }

        double promotionalPrice = 0.0;
        if (productClassificationsDoc.get("promotionalPrice") != null) {
          promotionalPrice = Double.parseDouble(
              productClassificationsDoc.get("promotionalPrice").toString());
        }

        storeIds.add(doc.getString("storeId"));

        return ProductOrderVm.builder().productId(doc.getObjectId("_id").toString())
            .name(doc.getString("name"))
            .optionId(productClassificationsDoc.getObjectId("_id").toString())
            .quantity(prodOrder.quantity()).price(
                Double.parseDouble(productClassificationsDoc.get("price").toString()))
            .promotionalPrice(promotionalPrice).image(doc.getList("images", String.class).get(0))
            .nameOption(productClassificationsDoc.getString("name")).totalPrice(
                productClassificationsDoc.get("promotionalPrice") == null ?
                    Double.parseDouble(productClassificationsDoc.get("price").toString())
                        * prodOrder.quantity()
                    : Double.parseDouble(
                        productClassificationsDoc.get("promotionalPrice").toString())
                        * prodOrder.quantity()).storeId(doc.getString("storeId")).build();
      }).toList();

      if (orders.size() != orderProductSet.size()) {
        throw new NotFoundException("Product not found");
      }
      String hash;
      try {
        hash = generateHash("store-service",
            getPrivateKey(secureInternalProperties.getPrivateKey()));
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

      var storeProvinceCodes = storeClient.getProvinceCodes(hash,
          GetProvinceCodesRequest.builder().ids(storeIds).build());

      long endTime = System.nanoTime();
      LOGGER.info(endTime - startTime + " ns");
      return OrderProductResponse.builder()
          .totalPrice(Math.ceil(orders.stream().mapToDouble(ProductOrderVm::totalPrice).sum()))
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
          product.getPrice(), product.getIsPublished(), product.getImages(),
          product.getDescription(),
          product.getProductClassifications(),
          product.getProductClassifications().stream().map(ProductClassification::getName).toList(),
          categoryNames, product.getDetailsProduct(), product.getIsBanned(), product.getIsDeleted(),
          product.getCreatedDate(), product.getCountSale(),
          calculateRatingAverage(product.getRatings()));
    });
  }

  @Override
  @Async
  public CompletableFuture<ProductDetailResponse> getProductDetail(String productId) {
    return CompletableFuture.supplyAsync(() -> {
      var product = productRepository.findById(productId)
          .orElseThrow(() -> new NotFoundException("PRODUCT_NOT_FOUND", productId));

      if (product.getIsBanned() || product.getIsDeleted()) {
        throw new NotFoundException("Product not found");
      }

      var categories = categoryRepository.findAllById(product.getCategories());

      var store = storeClient.getDetailStore(product.getStoreId());

      return new ProductDetailResponse(product.getId(), product.getName(), product.getSlug(),
          product.getPrice(), product.getPromotionalPrice(), product.getIsPublished(),
          product.getImages(), product.getDescription(), product.getProductClassifications(),
          product.getProductVariants(),
          categories.stream().map(categoryMapper::mapToCategoryResponse).toList(),
          product.getDetailsProduct(), product.getIsBanned(), product.getIsDeleted(),
          product.getCreatedDate(), store, product.getRatings());
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
          Product.class, Document.class);
      var documents = results.getMappedResults();

      if (documents.isEmpty()) {
        throw new NotFoundException("Product not found");
      }

      String hash;
      try {
        hash = generateHash("store-service",
            getPrivateKey(secureInternalProperties.getPrivateKey()));
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

      var storeIds = new HashSet<>(
          documents.stream().map(doc -> doc.getString("storeId")).toList());
      var stores = storeClient.getDetailStores(hash, MultipleStoreRequest.builder()
          .ids(storeIds).build());

      return documents.stream().map(doc -> {
        var productClassificationsDoc = doc.get("productClassifications", Document.class);
        var productSample = gson.fromJson(doc.toJson(), ProductSample.class);

        String image;
        if (productSample.productVariants().isEmpty()) {
          image = doc.getList("images", String.class).get(0);
        } else {
          image = productSample.productVariants().get(0).options().get(0).getImage();
        }
//        if (productSample.productVariants().isEmpty()) {
//          image = doc.getString("image");
//        } else {
//          int size = productSample.productVariants().get(0).options().size() * productSample.productVariants().size() == 2 ? productSample.productVariants().get(1)
//              .options().size() : 1;
//          image = productSample.productVariants().get(0).options().get(
//                  size / (Integer.parseInt(productClassificationsDoc.getString("code") + 1)))
//              .getImage();
//        }

        var store = stores.stream().filter(x -> x.id().equals(doc.getString("storeId"))).findFirst()
            .orElseThrow();
        return new ProductThumbnailVm(doc.getObjectId("_id").toString(), doc.getString("name"),
            productClassificationsDoc.getString("name"),
            Double.parseDouble(productClassificationsDoc.get("price").toString()),
            image, store.id(), store.name(), store.cityOrProvinceId());
      }).toList();
    });
  }

  @Override
  @Async
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
        if (product.getProductVariants().isEmpty()) {
          product.setQuantity(req.quantity());
        }

        for (ProductClassification productClassification : product.getProductClassifications()) {
          if (productClassification.getId().equals(req.optionId())) {
            if (productClassification.getQuantity() < req.quantity()) {
              throw new BadRequestException("Product out of stock");
            }
            productClassification.setQuantity(
                Math.max(productClassification.getQuantity() - req.quantity(), 0));
            product.setCountSale(Math.max(product.getCountSale() + req.quantity(), 0));
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
      for (var prod : products) {
        String message;
        try {
          message = ParseObjectToString.parse(
              new ProductMessageVm(prod.getId(), Action.UPDATE, null));
        } catch (JsonProcessingException e) {
          throw new RuntimeException(e);
        }
        rabbitMQProducer.sendMessage(exchange, routingKey, message);
      }
      return null;
    });
  }

  @Override
  @Async
  public CompletableFuture<ProductResponse> update(String userId, String id,
      UpdateProductRequest input, HttpServletRequest request) {
    var product = productRepository.findById(id)
        .orElseThrow(() -> new BadRequestException("Product not found"));

    // check permission to change product (store service)
    var store = storeClient.getStoreId(request.getHeader("Authorization"), userId);

    if (!store.equals(product.getStoreId())) {
      throw new BadRequestException("You don't have permission to change this product");
    }

    if (input.images().isEmpty()) {
      throw new BadRequestException("Product must have at least 1 image");
    }

    if (input.images().stream().anyMatch(String::isBlank)) {
      throw new BadRequestException("Image is required");
    }

    if (input.productVariants().isEmpty()) {

      checkProductNoOption(input.price());

      input.productClassifications().clear();
      input.productClassifications().add(
          ProductClassificationVm.builder().name("Default").price(input.price())
              .quantity(input.quantity()).build());
    } else {
      CheckProductHaveOption(input.productVariants(), input.productClassifications());
    }

    var savedProd = productMapper.mapToProductBeforeUpdate(product.getId(), input, store);
    savedProd.setIsPublished(input.isPublished());
    savedProd = productRepository.save(savedProd);

    // send message create message
    String message;
    try {
      message = ParseObjectToString.parse(
          new ProductMessageVm(savedProd.getId(), Action.UPDATE, null));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    rabbitMQProducer.sendMessage(exchange, routingKey, message);

    return CompletableFuture.completedFuture(productMapper.mapToProductToResponse(savedProd, null));
  }

  private void CheckProductHaveOption(List<ProductVariantVm> productVariantVms,
      List<ProductClassificationVm> productClassificationVms) {
    if (productVariantVms.size() > 2) {
      throw new BadRequestException("Product have maximum 2 variants");
    }

    productVariantVms.forEach(productVariantVm -> {
      if (productVariantVm.options().isEmpty()) {
        throw new BadRequestException("invalid value");
      }
    });

    productVariantVms.get(0).options().forEach(
        option -> {
          if (option == null || option.getImage().isBlank()) {
            throw new BadRequestException("Image is required");
          }
        }
    );

    if (productVariantVms.size() == 1) {
      if (productVariantVms.get(0).options().size() != productClassificationVms.size()) {
        throw new BadRequestException("Product classification must be filled");
      }
      for (int i = 0; i < productVariantVms.get(0).options().size(); i++) {
        if (productVariantVms.get(0).options().get(i).getValue().isBlank()) {
          throw new BadRequestException("Product classification must be filled");
        }

        productClassificationVms.set(i,
            ProductClassificationVm.setCodeName(productClassificationVms.get(i), String.valueOf(i),
                productVariantVms.get(0).options().get(i).getValue()));
      }
    } else {
      int count =
          productVariantVms.get(0).options().size() * productVariantVms.get(1).options().size();

      if (count != productClassificationVms.size()) {
        throw new BadRequestException("Product classification must be filled");
      }

      count = 0;

      for (int i = 0; i < productVariantVms.get(0).options().size(); i++) {
        if (productVariantVms.get(0).options().get(i).getValue().isBlank()) {
          throw new BadRequestException("Product classification must be filled");
        }
        for (int j = 0; j < productVariantVms.get(1).options().size(); j++) {
          if (productVariantVms.get(1).options().get(j).getValue().isBlank()) {
            throw new BadRequestException("Product classification must be filled");
          }
          productClassificationVms.set(i,
              ProductClassificationVm.setCodeName(productClassificationVms.get(i),
                  String.valueOf(i),
                  productVariantVms.get(0).options().get(i).getValue() + " - "
                      + productVariantVms.get(1)
                      .options().get(j).getValue()));

          count++;
        }
      }
    }
  }

  private void checkProductNoOption(Double price) {
    if (price == null || price <= 0) {
      throw new BadRequestException("Price must be greater than 0");
    }
  }

  @Override
  @Async
  public CompletableFuture<Void> remove(String userId, String id, HttpServletRequest request) {
    return CompletableFuture.supplyAsync(() -> {
      Product product = productRepository.findById(id)
          .orElseThrow(() -> new BadRequestException("Product not found"));
      // check permission to change product (store service)

      var store = storeClient.getStoreId(request.getHeader("Authorization"), userId);
      if (!store.equals(product.getStoreId())) {
        throw new BadRequestException("You don't have permission to change this product");
      }
      product.setIsDeleted(true);
      var savedProduct = productRepository.save(product);

      // send message create message
      String message;
      try {
        message = ParseObjectToString.parse(
            new ProductMessageVm(savedProduct.getId(), Action.UPDATE, null));
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
      rabbitMQProducer.sendMessage(exchange, routingKey, message);

      return null;
    });
  }

  @Override
  @Async
  public CompletableFuture<Void> ban(String id, BanProductRequest request) {
    return CompletableFuture.supplyAsync(() -> {

      var product = productRepository.findById(id)
          .orElseThrow(
              () -> new NotFoundException("Product not found"));
      if (product.getIsBanned().equals(request.isBanned())) {
        throw new BadRequestException("Product already banned");
      }
      product.setIsBanned(request.isBanned());
      if (request.isBanned()) {
        LOGGER.info("Product {} is banned with reason {}", id, request.reason());
        product.setReasonBan(request.reason());
      } else {
        LOGGER.info("Product {} is unbanned", id);
        product.setReasonBan(null);
      }
      productRepository.save(product);

      // send message create message
      try {
        String message = ParseObjectToString.parse(
            new ProductMessageVm(id, Action.BAN, request.isBanned()));
        rabbitMQProducer.sendMessage(exchange, routingKey, message);
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }

      return null;
    });
  }

  private TypedAggregation<ProductClassification> createQueryClassification(List<String> prods,
      List<String> options) {
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

  private TypedAggregation<ProductStoreResponse> createQueryProduct(long skip, int limit,
      String name, String orderBy, String storeId, Boolean ban, Boolean isDeleted) {
    Direction direction = orderBy.charAt(0) == '-' ? Direction.DESC : Direction.ASC;
    String orderByField = orderBy.charAt(0) == '-' ? orderBy.substring(1) : orderBy;
    return Aggregation.newAggregation(ProductStoreResponse.class, Aggregation.match(
            Criteria.where("storeId").is(storeId).and("isBanned").is(ban).and("isDeleted").is(isDeleted)
                .and("name").regex(name, "i")), Aggregation.skip(skip), Aggregation.limit(limit),
        Aggregation.sort(direction, orderByField));
  }

  @NotNull
  private PagedResultDto<ProductStoreResponse> getProductResponsePagedResultDto(long skip,
      int limit, long total, TypedAggregation<ProductStoreResponse> aggregate) {
    var results = mongoTemplate.aggregate(aggregate, Product.class, Document.class);
    var documents = results.getMappedResults();
    var list = documents.stream().map(doc -> {

      var prod = gson.fromJson(doc.toJson(), Product.class);

      var price =
          prod.getProductClassifications().get(0).getPromotionalPrice() != null
              && prod.getProductClassifications().get(0).getPromotionalPrice() > 0
              ? prod.getProductClassifications().get(0).getPromotionalPrice()
              : prod.getProductClassifications().get(0).getPrice();

      return ProductStoreResponse.builder().id(doc.getObjectId("_id").toString())
          .name(prod.getName()).image(prod.getImages().get(0))
          .countProductVariants(prod.getProductVariants().size())
          .countSale(doc.getInteger("countSale"))
          .reasonBan(doc.getString("reasonBan")).price(price)
          .rating(calculateRatingAverage(prod.getRatings())).build();

    }).toList();
    return PagedResultDto.create(Pagination.create(total, skip, limit), list);
  }

  public double calculateRatingAverage(List<Integer> ratings) {
    int totalWeight = 0;
    int weightedSum = 0;

    for (int i = 0; i < ratings.size(); i++) {
      int weight = i + 1;
      totalWeight += weight;
      weightedSum += weight * ratings.get(i);
    }

    return (double) weightedSum / totalWeight;
  }
}
