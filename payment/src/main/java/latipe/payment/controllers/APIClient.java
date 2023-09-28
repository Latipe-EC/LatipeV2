package latipe.payment.controllers;

import latipe.payment.request.TokenRequest;
import latipe.payment.response.UserCredentialResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "AUTH-SERVICE", url = "http://localhost:8181")
public interface APIClient {

  @PostMapping(value = "/api/v1/auth/validate-token")
  UserCredentialResponse getCredential(@RequestBody() TokenRequest accessToken);
}
