package latipe.product.services.product;

import latipe.product.Entity.Product;
import latipe.product.Entity.ProductClassification;
import latipe.product.controllers.APIClient;
import latipe.product.dtos.ProductPriceDto;
import latipe.product.exceptions.BadRequestException;
import latipe.product.exceptions.NotFoundException;
import latipe.product.repositories.IProductRepository;
import latipe.product.services.product.Dtos.ProductCreateDto;
import latipe.product.services.product.Dtos.ProductDto;
import latipe.product.services.product.Dtos.ProductUpdateDto;
import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class ProductService implements IProductService {
    private final IProductRepository productRepository;
    private final ModelMapper toDto;
    private final APIClient apiClient;

    public ProductService(IProductRepository productRepository, ModelMapper toDto, APIClient apiClient) {
        this.productRepository = productRepository;
        this.toDto = toDto;
        this.apiClient = apiClient;
    }

    @Override
    @Async
    public CompletableFuture<ProductDto> create(String userId, ProductCreateDto input) {
        return CompletableFuture.supplyAsync(() -> {
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
    public CompletableFuture<ProductDto> update(String id, ProductUpdateDto input) {
        // check permission to change product (store service)
        if (false) {
            throw new BadRequestException("You don't have permission to change this product");
        }

        Product product = productRepository.findById(id).orElseThrow(() -> new BadRequestException("Product not found"));
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
}
