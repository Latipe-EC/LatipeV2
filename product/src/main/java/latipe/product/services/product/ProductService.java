package latipe.product.services.product;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import latipe.product.Entity.Category;
import latipe.product.Entity.Product;
import latipe.product.Entity.ProductClassification;
import latipe.product.configs.CustomAggregationOperation;
import latipe.product.controllers.APIClient;
import latipe.product.request.ProductFeatureRequest;
import latipe.product.viewmodel.ProductPriceVm;
import latipe.product.exceptions.BadRequestException;
import latipe.product.exceptions.NotFoundException;
import latipe.product.mapper.ProductMapper;
import latipe.product.repositories.ICategoryRepository;
import latipe.product.repositories.IProductRepository;
import latipe.product.request.BanProductRequest;
import latipe.product.request.CreateProductRequest;
import latipe.product.request.OrderProductCheckRequest;
import latipe.product.request.UpdateProductRequest;
import latipe.product.response.OrderProductResponse;
import latipe.product.response.ProductResponse;
import latipe.product.viewmodel.ProductOrderVm;
import latipe.product.viewmodel.ProductESDetailVm;
import latipe.product.viewmodel.ProductThumbnailVm;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ProductService implements IProductService {
    private final IProductRepository productRepository;
    private final ICategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final APIClient apiClient;
    private final MongoTemplate mongoTemplate;

    public ProductService(IProductRepository productRepository,
        ICategoryRepository categoryRepository, ProductMapper productMapper, APIClient apiClient,
        MongoTemplate mongoTemplate) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productMapper = productMapper;
        this.apiClient = apiClient;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    @Async
    public CompletableFuture<ProductResponse> create(String userId, CreateProductRequest input) {
        return CompletableFuture.supplyAsync(() -> {
            if (input.productVariant().isEmpty()) {
                if (input.price() == null || input.price() <= 0) {
                    throw new BadRequestException("Price must be greater than 0");
                }
                if (input.quantity() <= 0) {
                    throw new BadRequestException("Quantity must be greater than 0");
                }
                if (input.images().isEmpty()) {
                    throw new BadRequestException("Product must have at least 1 image");
                }
                input.productClassifications().add(ProductClassification.builder()
                        .name("Default")
                        .price(input.price())
                        .quantity(input.quantity())
                        .image(input.images().get(0))
                        .build());
            } else {
                if (input.productVariant().size() > 2) {
                    throw new BadRequestException("Product have maximum 2 variants");
                }
                if (input.productVariant().size() == 1) {
                    if (input.productVariant().get(0).getOptions().size() != input.productClassifications().size()) {
                        throw new BadRequestException("Product classification must be filled");
                    }
                    for (int i = 0; i < input.productVariant().get(0).getOptions().size(); i++) {
                        input.productClassifications().get(i).setCode(String.valueOf(i));
                    }
                } else {
                    int count = input.productVariant().get(0).getOptions().size() * input.productVariant().get(1).getOptions().size();
                    if (count != input.productClassifications().size()) {
                        throw new BadRequestException("Product classification must be filled");
                    }
                    count = 0;
                    for (int i = 0; i < input.productVariant().get(0).getOptions().size(); i++) {
                        for (int j = 0; j < input.productVariant().get(1).getOptions().size(); j++) {
                            input.productClassifications().get(count).setCode(String.valueOf(i) + String.valueOf(j));
                            input.productClassifications().get(count).setName(input.productVariant().get(0).getOptions().get(i) + " - " + input.productVariant().get(1).getOptions().get(j));
                            count++;
                        }
                    }
                }

            }
            var prod = productMapper.mapToProductBeforeCreate(input);
            // get store id from store service
            prod.setStoreId(apiClient.getStoreId(userId));
            var savedProd = productRepository.save(prod);

            return productMapper.mapToProductToResponse(savedProd);
        });

    }

    @Override
    @Async
    public CompletableFuture<ProductPriceVm> getPrice(String prodId, String code) {
        return CompletableFuture.supplyAsync(() -> {
            Product product = productRepository.findById(prodId).orElseThrow(() -> new BadRequestException("Product not found"));
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
    public CompletableFuture<OrderProductResponse> checkProductInStock(List<OrderProductCheckRequest> prodOrders) {
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
            var aggregate = createQueryClassification(prodOrders.stream().map(OrderProductCheckRequest::productId).toList(),
                    prodOrders.stream().map(OrderProductCheckRequest::optionId).toList());
            AggregationResults<Document> results = mongoTemplate.aggregate(aggregate, ProductClassification.class, Document.class);
            List<Document> documents = results.getMappedResults();
            List<ProductOrderVm> orders = documents.stream()
                    .map(doc -> {
                        Document productClassificationsDoc = doc.get("productClassifications", Document.class);
                        OrderProductCheckRequest prodOrder = prodOrders.stream().filter(
                                        x -> x.productId()
                                                     .equals(doc.getString("_id")) && x.optionId().equals(productClassificationsDoc.getString("_id")))
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

                    List<String> categoryNames = categoryRepository.findAllById(product.getCategories()).stream().map(Category::getName).toList();
                    return new ProductESDetailVm(
                            product.getId(),
                            product.getName(),
                            product.getSlug(),
                            product.getPrice(),
                            product.isPublished(),
                            product.getImages(),
                            product.getDescription(),
                            product.getProductClassifications(),
                            product.getProductClassifications().stream().map(ProductClassification::getName).toList(),
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
    public CompletableFuture<List<ProductThumbnailVm>> getFeatureProduct(List<ProductFeatureRequest> products) {
        return CompletableFuture.supplyAsync(() -> {
            var aggregate = createQueryClassification(products.stream().map(ProductFeatureRequest::productId).toList(), products.stream().map(
                ProductFeatureRequest::optionId).toList());
            AggregationResults<Document> results = mongoTemplate.aggregate(aggregate, ProductClassification.class, Document.class);
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
    public CompletableFuture<ProductResponse> update(String userId, String id, UpdateProductRequest input) {
        Product product = productRepository.findById(id).orElseThrow(() -> new BadRequestException("Product not found"));
        // check permission to change product (store service)
        if (!apiClient.getStoreId(userId).equals(product.getStoreId())) {
            throw new BadRequestException("You don't have permission to change this product");
        }
        if (input.productVariant().size() == 0) {
            if (input.price() == null || input.price() <= 0) {
                throw new BadRequestException("Price must be greater than 0");
            }
            if (input.quantity() <= 0) {
                throw new BadRequestException("Quantity must be greater than 0");
            }
            if (input.images().size() == 0) {
                throw new BadRequestException("Product must have at least 1 image");
            }
            input.productClassifications().add(ProductClassification.builder()
                    .name("Default")
                    .price(input.price())
                    .quantity(input.quantity())
                    .image(input.images().get(0))
                    .build());
        } else {
            if (input.productVariant().size() > 2) {
                throw new BadRequestException("Product have maximum 2 variants");
            }
            if (input.productVariant().size() == 1) {
                if (input.productVariant().get(0).getOptions().size() != input.productClassifications().size()) {
                    throw new BadRequestException("Product classification must be filled");
                }
                for (int i = 0; i < input.productVariant().get(0).getOptions().size(); i++) {
                    input.productClassifications().get(i).setCode(String.valueOf(i));
                }
            } else {
                int count = input.productVariant().get(0).getOptions().size() * input.productVariant().get(1).getOptions().size();
                if (count != input.productClassifications().size()) {
                    throw new BadRequestException("Product classification must be filled");
                }
                count = 0;
                for (int i = 0; i < input.productVariant().get(0).getOptions().size(); i++) {
                    for (int j = 0; j < input.productVariant().get(1).getOptions().size(); j++) {
                        input.productClassifications().get(count).setCode(String.valueOf(i) + String.valueOf(j));
                        count++;
                    }
                }
            }
        }
        var savedProd = productRepository.save(product);

        return CompletableFuture.completedFuture(productMapper.mapToProductToResponse(savedProd));
    }

    @Override
    @Async
    public CompletableFuture<Void> remove(String userId, String id) {
        return CompletableFuture.supplyAsync(() -> {
            Product product = productRepository.findById(id).orElseThrow(() -> new BadRequestException("Product not found"));
            // check permission to change product (store service)
            if (!apiClient.getStoreId(userId).equals(product.getStoreId())) {
                throw new BadRequestException("You don't have permission to change this product");
            }
            product.setDeleted(true);
            productRepository.save(product);
            return null;
        });
    }

    @Override
    @Async
    public CompletableFuture<Void> ban(String id, BanProductRequest input) {
        return CompletableFuture.supplyAsync(() -> {
            Product product = productRepository.findById(id).orElseThrow(() -> new BadRequestException("Product not found"));
            // check permission to change product (store service)
            product.setReasonBan(input.reason());
            product.setBanned(true);
            productRepository.save(product);
            return null;
        });
    }

    private TypedAggregation<ProductClassification> createQueryClassification(List<String> prods, List<String> options) {
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
