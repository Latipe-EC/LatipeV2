package latipe.cart.services.Product;

import latipe.cart.controllers.APIClient;
import latipe.cart.request.ProductFeatureRequest;
import latipe.cart.response.ProductThumbnailResponse;
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
    public CompletableFuture<List<ProductThumbnailResponse>> getProducts(List<ProductFeatureRequest> ids) {
        return CompletableFuture.supplyAsync(
                () -> apiClient.getProducts(ids)
        );
    }
}
