package latipe.search.feign;

import feign.Param;
import feign.RequestLine;
import latipe.search.viewmodel.ProductESDetailVm;

public interface ProductClient {

    @RequestLine("GET /products/products-es/{productId}")
    ProductESDetailVm getProductESDetailById(@Param("productId") String productId);
}
