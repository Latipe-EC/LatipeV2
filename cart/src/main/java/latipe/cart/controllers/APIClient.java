package latipe.cart.controllers;

import latipe.cart.dtos.TokenDto;
import latipe.cart.dtos.UserCredentialDto;
import latipe.cart.viewmodel.ProductThumbnailVm;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "API", url = "http://localhost:8181/api/v1")
public interface APIClient {
    @PostMapping(value = "/auth/validate-token")
    UserCredentialDto getCredential(@RequestBody() TokenDto accessToken);

    @GetMapping(value = "/products/list-featured")
    List<ProductThumbnailVm> getProducts(@RequestBody() List<String> ids);
}
