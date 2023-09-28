package latipe.search.controllers;

import latipe.search.request.TokenRequest;
import latipe.search.response.UserCredentialResponse;
import latipe.search.viewmodel.ProductESDetailVm;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "API", url = "http://localhost:8181/api/v1")
public interface APIClient {

  @PostMapping(value = "/auth/validate-token")
  UserCredentialResponse getCredential(@RequestBody() TokenRequest accessToken);

  @GetMapping(value = "/products-es/{productId}")
  ProductESDetailVm getProductESDetailById(@PathVariable("productId") String productId);
}
