package latipe.product.controllers;

import latipe.product.dtos.TokenDto;
import latipe.product.dtos.UserCredentialDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name="AUTH-SERVICE")
public interface APIClient {
    @PostMapping(value = "http://localhost:8181/api/v1/auth/validate-token")
    UserCredentialDto getCredential(@RequestBody() TokenDto accessToken);

    @GetMapping(value = "http://localhost:8118/api/v1/store/validate-store/{userId}")
    String getStoreId(@PathVariable String userId);
}
