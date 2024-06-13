package latipe.cart.feign;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import jakarta.validation.Valid;
import java.util.List;
import latipe.cart.request.ProductFeatureRequest;
import latipe.cart.response.ProductThumbnailResponse;
import org.springframework.web.bind.annotation.RequestBody;

public interface ProductClient {

    @RequestLine("POST /products/list-featured")
    @Headers({"X-API-KEY: {requester}", "Content-Type: application/json"})
    List<ProductThumbnailResponse> getProducts(@Param("requester") String requester,
        @RequestBody List<@Valid ProductFeatureRequest> request);

}
