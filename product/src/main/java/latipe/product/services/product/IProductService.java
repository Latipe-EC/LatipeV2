package latipe.product.services.product;


import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import latipe.product.constants.EStatusBan;
import latipe.product.dtos.PagedResultDto;
import latipe.product.request.BanProductRequest;
import latipe.product.request.CreateProductRequest;
import latipe.product.request.OrderProductCheckRequest;
import latipe.product.request.ProductESDetailsRequest;
import latipe.product.request.ProductFeatureRequest;
import latipe.product.request.UpdateProductQuantityRequest;
import latipe.product.request.UpdateProductRequest;
import latipe.product.response.OrderProductResponse;
import latipe.product.response.ProductAdminResponse;
import latipe.product.response.ProductDetailResponse;
import latipe.product.response.ProductESDetailsResponse;
import latipe.product.response.ProductListGetResponse;
import latipe.product.response.ProductNameListResponse;
import latipe.product.response.ProductResponse;
import latipe.product.response.ProductStoreResponse;
import latipe.product.viewmodel.ProductESDetailVm;
import latipe.product.viewmodel.ProductPriceVm;
import latipe.product.viewmodel.ProductThumbnailVm;
import org.springframework.web.bind.annotation.RequestParam;

public interface IProductService {


    CompletableFuture<OrderProductResponse> checkProductInStock(
        List<OrderProductCheckRequest> prodOrders, HttpServletRequest request);

    CompletableFuture<ProductPriceVm> getPrice(String prodId, String code,
        HttpServletRequest request);

    CompletableFuture<Long> countAllProduct(HttpServletRequest request);

    CompletableFuture<ProductResponse> update(String id, UpdateProductRequest input,
        HttpServletRequest request);

    CompletableFuture<Void> remove(String id, HttpServletRequest request);

    CompletableFuture<Void> ban(String id, BanProductRequest input, HttpServletRequest request);

    CompletableFuture<List<ProductThumbnailVm>> getFeatureProduct(
        List<ProductFeatureRequest> products, HttpServletRequest request);

    CompletableFuture<ProductESDetailVm> getProductESDetailById(String productId,
        HttpServletRequest request);

    CompletableFuture<ProductResponse> create(CreateProductRequest input,
        HttpServletRequest request);

    CompletableFuture<ProductResponse> get(String prodId, HttpServletRequest request);

    CompletableFuture<Void> updateQuantity(List<UpdateProductQuantityRequest> input,
        HttpServletRequest request);

    CompletableFuture<PagedResultDto<ProductStoreResponse>> getMyProductStore(long skip, int limit,
        String name, String orderBy, String storeId, HttpServletRequest request);

    CompletableFuture<PagedResultDto<ProductStoreResponse>> getBanProductStore(long skip, int limit,
        String name, String orderBy, String storeId, HttpServletRequest request);

    CompletableFuture<PagedResultDto<ProductAdminResponse>> getAdminProduct(long skip, int limit,
        String name, String orderBy, EStatusBan statusBan, HttpServletRequest request);

    CompletableFuture<ProductDetailResponse> getProductDetail(String productId,
        HttpServletRequest request);

    CompletableFuture<ProductListGetResponse> findProductAdvance(
        @RequestParam(defaultValue = "") String keyword,
        @RequestParam(defaultValue = "0") Integer page,
        @RequestParam(defaultValue = "12") Integer size,
        @RequestParam(required = false) String category
        , HttpServletRequest request);

    CompletableFuture<ProductNameListResponse> autoCompleteProductName(String keyword,
        HttpServletRequest request);

    CompletableFuture<List<ProductESDetailsResponse>> getProductESDetails(
        ProductESDetailsRequest input,
        HttpServletRequest request);
}
