package latipe.store.feign;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import latipe.store.dtos.PagedResultDto;
import latipe.store.response.product.ProductStoreResponse;

public interface ProductClient {

    @RequestLine("GET /products/store/{storeId}?name={name}&skip={skip}&size={size}&orderBy={orderBy}")
    @Headers({"X-API-KEY: {requester}", "Content-Type: application/json"})
    PagedResultDto<ProductStoreResponse> getProductStore(@Param("requester") String requester,
        @Param("name") String name, @Param("skip") long skip, @Param("size") int size,
        @Param("orderBy") String orderBy, @Param("storeId") String storeId);

    @RequestLine("GET /products/store/{storeId}/ban?name={name}&skip={skip}&size={size}&orderBy={orderBy}")
    @Headers({"X-API-KEY: {requester}", "Content-Type: application/json"})
    PagedResultDto<ProductStoreResponse> getBanProductStore(@Param("requester") String requester,
        @Param("name") String name, @Param("skip") long skip, @Param("size") int size,
        @Param("orderBy") String orderBy, @Param("storeId") String storeId);
}
