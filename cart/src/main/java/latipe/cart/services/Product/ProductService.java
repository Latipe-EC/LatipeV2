package latipe.cart.services.Product;

import latipe.cart.controllers.APIClient;
import latipe.cart.viewmodel.ProductThumbnailVm;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class ProductService {
    private final APIClient apiClient;

    public ProductService(APIClient apiClient) {
        this.apiClient = apiClient;
    }

    @Async
    public CompletableFuture<List<ProductThumbnailVm>> getProducts(List<String> ids) {
        return CompletableFuture.supplyAsync(
                () -> apiClient.getProducts(ids)
        );
    }
}
