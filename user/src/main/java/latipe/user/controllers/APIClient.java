package latipe.user.controllers;

import latipe.user.request.TokenRequest;
import latipe.user.response.UserCredentialResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "API", url = "http://localhost:8181/api/v1")
public interface APIClient {

  @PostMapping(value = "/auth/validate-token")
  UserCredentialResponse getCredential(@RequestBody() TokenRequest accessToken);
}
