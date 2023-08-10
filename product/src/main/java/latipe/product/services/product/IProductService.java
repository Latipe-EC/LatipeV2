package latipe.product.services.product;


import latipe.product.dtos.ProductPriceDto;
import latipe.product.services.IService;
import latipe.product.services.product.Dtos.ProductCreateDto;
import latipe.product.services.product.Dtos.ProductDto;
import latipe.product.services.product.Dtos.ProductUpdateDto;
import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.CompletableFuture;

public interface IProductService extends IService<ProductDto, ProductCreateDto, ProductUpdateDto> {
    @Async
    CompletableFuture<ProductDto> create(String userId, ProductCreateDto input);

    public CompletableFuture<ProductPriceDto> getPrice(String prodId, String code);
//    @Query("{'id' : ?0}")
//    Product findById(String id);
}

