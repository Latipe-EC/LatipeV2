package latipe.product.services.product;

import latipe.product.Entity.Category;
import latipe.product.Entity.Product;
import latipe.product.Entity.ProductClassification;
import latipe.product.configs.CustomAggregationOperation;
import latipe.product.controllers.APIClient;
import latipe.product.dtos.ProductFeatureDto;
import latipe.product.dtos.ProductPriceDto;
import latipe.product.exceptions.BadRequestException;
import latipe.product.exceptions.NotFoundException;
import latipe.product.repositories.ICategoryRepository;
import latipe.product.repositories.IProductRepository;
import latipe.product.services.product.Dtos.*;
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

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class ProductService implements IProductService {
    private final IProductRepository productRepository;
    private final ICategoryRepository categoryRepository;
    private final ModelMapper toDto;
    private final APIClient apiClient;
    private final MongoTemplate mongoTemplate;

    public ProductService(IProductRepository productRepository, ICategoryRepository categoryRepository, ModelMapper toDto, APIClient apiClient, MongoTemplate mongoTemplate) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.toDto = toDto;
        this.apiClient = apiClient;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    @Async
    public CompletableFuture<ProductDto> create(String userId, ProductCreateDto input) {
        return CompletableFuture.supplyAsync(() -> {
            if (input.getProductVariant().isEmpty()) {
                if (input.getPrice() == null || input.getPrice() <= 0) {
                    throw new BadRequestException("Price must be greater than 0");
                }
                if (input.getQuantity() <= 0) {
                    throw new BadRequestException("Quantity must be greater than 0");
                }
                if (input.getImages().isEmpty()) {
                    throw new BadRequestException("Product must have at least 1 image");
                }
                input.getProductClassifications().add(ProductClassification.builder()
                        .name("Default")
                        .price(input.getPrice())
                        .quantity(input.getQuantity())
                        .image(input.getImages().get(0))
                        .build());
            } else {
                if (input.getProductVariant().size() > 2) {
                    throw new BadRequestException("Product have maximum 2 variants");
                }
                if (input.getProductVariant().size() == 1) {
                    if (input.getProductVariant().get(0).getOptions().size() != input.getProductClassifications().size()) {
                        throw new BadRequestException("Product classification must be filled");
                    }
                    for (int i = 0; i < input.getProductVariant().get(0).getOptions().size(); i++) {
                        input.getProductClassifications().get(i).setCode(String.valueOf(i));
                    }
                } else {
                    int count = input.getProductVariant().get(0).getOptions().size() * input.getProductVariant().get(1).getOptions().size();
                    if (count != input.getProductClassifications().size()) {
                        throw new BadRequestException("Product classification must be filled");
                    }
                    count = 0;
                    for (int i = 0; i < input.getProductVariant().get(0).getOptions().size(); i++) {
                        for (int j = 0; j < input.getProductVariant().get(1).getOptions().size(); j++) {
                            input.getProductClassifications().get(count).setCode(String.valueOf(i) + String.valueOf(j));
                            input.getProductClassifications().get(count).setName(input.getProductVariant().get(0).getOptions().get(i) + " - " + input.getProductVariant().get(1).getOptions().get(j));
                            count++;
                        }
                    }
                }

            }
            Product prod = toDto.map(input, Product.class);
            // get store id from store service
            prod.setStoreId(apiClient.getStoreId(userId));
            return toDto.map(productRepository.save(prod), ProductDto.class);
        });

    }

    @Override
    @Async
    public CompletableFuture<ProductPriceDto> getPrice(String prodId, String code) {
        return CompletableFuture.supplyAsync(() -> {
            Product product = productRepository.findById(prodId).orElseThrow(() -> new BadRequestException("Product not found"));
            if (product.getPrice() > 0) {
                throw new BadRequestException("Product is not have variant");
            }
            for (ProductClassification classification : product.getProductClassifications()) {
                if (classification.getCode().equals(code)) {
                    ProductPriceDto productPriceDto;
                    productPriceDto = ProductPriceDto.builder()
                            .code(code)
                            .image(classification.getImage())
                            .price(classification.getPrice())
                            .quantity(classification.getQuantity())
                            .build();
                    return productPriceDto;
                }
            }
            throw new NotFoundException("Product classification not found");
        });
    }


    @Override
    @Async
    public CompletableFuture<OrderProductResultsDto> checkProductInStock(List<OrderProductCheckDto> prodOrders) {
        return CompletableFuture.supplyAsync(() -> {
            double total = 0;
//            String query =
//                    """
//                                {
//                                    "$unwind": "$productClassifications"
//                                }
//                            """;
//            TypedAggregation<ProductClassification> test = Aggregation.newAggregation(
//                    ProductClassification.class,
//                    Aggregation.match(Criteria.where("_id").in(prodOrders.stream().map(OrderProductCheckDto::getProductId).toList())),
//                    // unwind product variant
//                    new CustomAggregationOperation(query),
//                    Aggregation.match(Criteria.where("productClassifications._id").in(prodOrders.stream().map(OrderProductCheckDto::getOptionId).toList()))
//            );
            var aggregate = createQueryClassification(prodOrders.stream().map(OrderProductCheckDto::getProductId).toList(),
                    prodOrders.stream().map(OrderProductCheckDto::getOptionId).toList());
            AggregationResults<Document> results = mongoTemplate.aggregate(aggregate, ProductClassification.class, Document.class);
            List<Document> documents = results.getMappedResults();
            List<ProductOrderDto> orders = documents.stream()
                    .map(doc -> {
                        Document productClassificationsDoc = doc.get("productClassifications", Document.class);
                        OrderProductCheckDto prodOrder = prodOrders.stream().filter(
                                        x -> x.getProductId()
                                                     .equals(doc.getString("_id")) && x.getOptionId().equals(productClassificationsDoc.getString("_id")))
                                .findFirst().orElseThrow(() -> new BadRequestException("Product not found"));
                        if (productClassificationsDoc.getInteger("quantity") < prodOrder.getQuantity()) {
                            throw new BadRequestException("Product out of stock");
                        }
                        return ProductOrderDto.builder()
                                .productId(doc.getString("_id"))
                                .optionId(productClassificationsDoc.getString("_id"))
                                .quantity(prodOrder.getQuantity())
                                .price(productClassificationsDoc.getDouble("price"))
                                .nameOption(productClassificationsDoc.getString("name"))
                                .totalPrice(productClassificationsDoc.getDouble("price") * prodOrder.getQuantity())
                                .build();
                    }).toList();
            return OrderProductResultsDto.builder()
                    .totalPrice(orders.stream().mapToDouble(ProductOrderDto::getTotalPrice).sum())
                    .products(orders)
                    .build();

//            List<Product> products = productRepository.findByIds(prodOrders.stream().map(OrderProductCheckDto::getProductId).toList());
//            if (products.size() != prodOrders.size()) {
//                throw new BadRequestException("Product not found");
//            }
//            List<ProductOrderDto> orders = new ArrayList<>();
//            for (OrderProductCheckDto prodOrder : prodOrders) {
//                ProductClassification prodVariant = products.stream().flatMap(product -> product.getProductClassifications().stream())
//                        .filter(classification -> classification.getId().equals(prodOrder.getOptionId()))
//                        .findFirst()
//                        .orElseThrow(() -> new BadRequestException("Product classification not found"));
//                if (prodVariant.getQuantity() < prodOrder.getQuantity()) {
//                    throw new BadRequestException("Product out of stock");
//                }
//                orders.add(ProductOrderDto.builder()
//                        .productId(prodOrder.getProductId())
//                        .optionId(prodOrder.getOptionId())
//                        .quantity(prodOrder.getQuantity())
//                        .price(prodVariant.getPrice())
//                        .nameOption(prodVariant.getName())
//                        .build());
//                total += prodVariant.getPrice() * prodOrder.getQuantity();
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
    public CompletableFuture<List<ProductThumbnailVm>> getFeatureProduct(List<ProductFeatureDto> products) {
        return CompletableFuture.supplyAsync(() -> {
            var aggregate = createQueryClassification(products.stream().map(ProductFeatureDto::getProductId).toList(), products.stream().map(ProductFeatureDto::getOptionId).toList());
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
    public CompletableFuture<ProductDto> update(String userId, String id, ProductUpdateDto input) {
        Product product = productRepository.findById(id).orElseThrow(() -> new BadRequestException("Product not found"));
        // check permission to change product (store service)
        if (!apiClient.getStoreId(userId).equals(product.getStoreId())) {
            throw new BadRequestException("You don't have permission to change this product");
        }
        if (input.getProductVariant().size() == 0) {
            if (input.getPrice() == null || input.getPrice() <= 0) {
                throw new BadRequestException("Price must be greater than 0");
            }
            if (input.getQuantity() <= 0) {
                throw new BadRequestException("Quantity must be greater than 0");
            }
            if (input.getImages().size() == 0) {
                throw new BadRequestException("Product must have at least 1 image");
            }
            input.getProductClassifications().add(ProductClassification.builder()
                    .name("Default")
                    .price(input.getPrice())
                    .quantity(input.getQuantity())
                    .image(input.getImages().get(0))
                    .build());
        } else {
            if (input.getProductVariant().size() > 2) {
                throw new BadRequestException("Product have maximum 2 variants");
            }
            if (input.getProductVariant().size() == 1) {
                if (input.getProductVariant().get(0).getOptions().size() != input.getProductClassifications().size()) {
                    throw new BadRequestException("Product classification must be filled");
                }
                for (int i = 0; i < input.getProductVariant().get(0).getOptions().size(); i++) {
                    input.getProductClassifications().get(i).setCode(String.valueOf(i));
                }
            } else {
                int count = input.getProductVariant().get(0).getOptions().size() * input.getProductVariant().get(1).getOptions().size();
                if (count != input.getProductClassifications().size()) {
                    throw new BadRequestException("Product classification must be filled");
                }
                count = 0;
                for (int i = 0; i < input.getProductVariant().get(0).getOptions().size(); i++) {
                    for (int j = 0; j < input.getProductVariant().get(1).getOptions().size(); j++) {
                        input.getProductClassifications().get(count).setCode(String.valueOf(i) + String.valueOf(j));
                        count++;
                    }
                }
            }
        }
        return CompletableFuture.supplyAsync(() -> toDto.map(productRepository.save(product), ProductDto.class));
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
    public CompletableFuture<Void> ban(String id, BanProductDto input) {
        return CompletableFuture.supplyAsync(() -> {
            Product product = productRepository.findById(id).orElseThrow(() -> new BadRequestException("Product not found"));
            // check permission to change product (store service)
            product.setReasonBan(input.getReason());
            product.setBanned(true);
            productRepository.save(product);
            return null;
        });
    }

    @Override
    public CompletableFuture<Void> remove(String id) {
        return null;
    }

    @Override
    public CompletableFuture<List<ProductDto>> getAll() {
        return null;
    }

    @Override
    public CompletableFuture<ProductDto> getOne(String id) {
        return null;
    }

    @Override
    public CompletableFuture<ProductDto> create(ProductCreateDto input) {
        return null;
    }

    @Override
    public CompletableFuture<ProductDto> update(String id, ProductUpdateDto input) throws InvocationTargetException, IllegalAccessException {
        return null;
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
