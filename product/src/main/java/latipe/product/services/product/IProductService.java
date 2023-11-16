package latipe.product.services.product;


import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import latipe.product.dtos.PagedResultDto;
import latipe.product.request.BanProductRequest;
import latipe.product.request.CreateProductRequest;
import latipe.product.request.OrderProductCheckRequest;
import latipe.product.request.ProductFeatureRequest;
import latipe.product.request.UpdateProductQuantityRequest;
import latipe.product.request.UpdateProductRequest;
import latipe.product.response.OrderProductResponse;
import latipe.product.response.ProductDetailResponse;
import latipe.product.response.ProductResponse;
import latipe.product.response.ProductStoreResponse;
import latipe.product.viewmodel.ProductESDetailVm;
import latipe.product.viewmodel.ProductPriceVm;
import latipe.product.viewmodel.ProductThumbnailVm;

public interface IProductService {


  CompletableFuture<OrderProductResponse> checkProductInStock(
      List<OrderProductCheckRequest> prodOrders);

  CompletableFuture<ProductPriceVm> getPrice(String prodId, String code);

  CompletableFuture<ProductResponse> update(String userId, String id, UpdateProductRequest input,
      HttpServletRequest request);

  CompletableFuture<Void> remove(String userId, String id, HttpServletRequest request);

  CompletableFuture<Void> ban(String id, BanProductRequest input);

  CompletableFuture<List<ProductThumbnailVm>> getFeatureProduct(
      List<ProductFeatureRequest> products);

  CompletableFuture<ProductESDetailVm> getProductESDetailById(String productId);

  CompletableFuture<ProductResponse> create(String userId, CreateProductRequest input,
      HttpServletRequest request);

  CompletableFuture<ProductResponse> get(String userId, String prodId, HttpServletRequest request);

  CompletableFuture<Void> updateQuantity(List<UpdateProductQuantityRequest> request);

  CompletableFuture<PagedResultDto<ProductStoreResponse>> getMyProductStore(long skip, int limit,
      String name, String orderBy, String storeId);

  CompletableFuture<PagedResultDto<ProductStoreResponse>> getBanProductStore(long skip, int limit,
      String name, String orderBy, String storeId);

  CompletableFuture<ProductDetailResponse> getProductDetail(String productId);
}
