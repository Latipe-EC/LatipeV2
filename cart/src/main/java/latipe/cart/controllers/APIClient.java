package latipe.cart.controllers;

import java.util.List;
import latipe.cart.request.ProductFeatureRequest;
import latipe.cart.request.TokenRequest;
import latipe.cart.response.UserCredentialResponse;
import latipe.cart.response.ProductThumbnailResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "API", url = "http://localhost:8181/api/v1")
public interface APIClient {
    @PostMapping(value = "/auth/validate-token")
    UserCredentialResponse getCredential(@RequestBody() TokenRequest accessToken);

    @GetMapping(value = "/products/list-featured")
    List<ProductThumbnailResponse> getProducts(@RequestBody() List<ProductFeatureRequest> ids);
}
