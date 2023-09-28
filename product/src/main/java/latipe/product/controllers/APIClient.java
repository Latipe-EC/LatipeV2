package latipe.product.controllers;

import latipe.product.request.TokenRequest;
import latipe.product.response.UserCredentialResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name="AUTH-SERVICE", url = "http://localhost:8181/api/v1")
public interface APIClient {
    @PostMapping(value = "/auth/validate-token")
    UserCredentialResponse getCredential(@RequestBody() TokenRequest accessToken);

    @GetMapping(value = "/stores/validate-store/{userId}")
    String getStoreId(@PathVariable String userId);
}
