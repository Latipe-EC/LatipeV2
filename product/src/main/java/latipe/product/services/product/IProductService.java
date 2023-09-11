package latipe.product.services.product;


import latipe.product.dtos.ProductPriceDto;
import latipe.product.services.IService;
import latipe.product.services.product.Dtos.*;
import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IProductService extends IService<ProductDto, ProductCreateDto, ProductUpdateDto> {
    @Async
    CompletableFuture<ProductDto> create(String userId, ProductCreateDto input);
    public CompletableFuture<OrderProductResultsDto> checkProductInStock(List<OrderProductCheckDto> prodOrders);
    public CompletableFuture<ProductPriceDto> getPrice(String prodId, String code);
    public CompletableFuture<ProductDto> update(String userId, String id, ProductUpdateDto input);
    public CompletableFuture<Void> remove(String userId, String id);
    public CompletableFuture<Void> ban(String id, BanProductDto input);
}

