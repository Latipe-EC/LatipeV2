package latipe.product.services.product;


import jakarta.servlet.http.HttpServletRequest;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import latipe.product.request.BanProductRequest;
import latipe.product.request.CreateProductRequest;
import latipe.product.request.OrderProductCheckRequest;
import latipe.product.request.ProductFeatureRequest;
import latipe.product.request.UpdateProductRequest;
import latipe.product.response.OrderProductResponse;
import latipe.product.response.ProductResponse;
import latipe.product.viewmodel.ProductESDetailVm;
import latipe.product.viewmodel.ProductPriceVm;
import latipe.product.viewmodel.ProductThumbnailVm;
import org.springframework.scheduling.annotation.Async;

public interface IProductService {


  public CompletableFuture<OrderProductResponse> checkProductInStock(
      List<OrderProductCheckRequest> prodOrders);

  public CompletableFuture<ProductPriceVm> getPrice(String prodId, String code);

  public CompletableFuture<ProductResponse> update(String userId, String id,
      UpdateProductRequest input, HttpServletRequest request);

  public CompletableFuture<Void> remove(String userId, String id, HttpServletRequest request);

  public CompletableFuture<Void> ban(String id, BanProductRequest input);

  public CompletableFuture<List<ProductThumbnailVm>> getFeatureProduct(
      List<ProductFeatureRequest> products);

  public CompletableFuture<ProductESDetailVm> getProductESDetailById(String productId);

  public CompletableFuture<ProductResponse> create(String userId, CreateProductRequest input, HttpServletRequest request);
}
