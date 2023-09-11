package latipe.category.controllers;

import latipe.category.dtos.TokenDto;
import latipe.category.dtos.UserCredentialDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "AUTH-SERVICE", url = "http://localhost:8181")
public interface APIClient {
    @PostMapping(value = "/api/v1/auth/validate-token")
    UserCredentialDto getCredential(@RequestBody() TokenDto accessToken);
}
