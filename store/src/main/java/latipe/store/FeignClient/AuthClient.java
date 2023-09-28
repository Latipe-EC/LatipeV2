package latipe.store.FeignClient;

import feign.Headers;
import feign.RequestLine;
import latipe.store.request.TokenRequest;
import latipe.store.response.UserCredentialResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface AuthClient {

  @RequestLine("POST /auth/validate-token")
  @Headers("Content-Type: application/json")
  UserCredentialResponse getCredential(TokenRequest accessToken);
}
