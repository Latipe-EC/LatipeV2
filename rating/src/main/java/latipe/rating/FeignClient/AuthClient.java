package latipe.rating.FeignClient;

import feign.Headers;
import feign.RequestLine;

public interface AuthClient {

  @RequestLine("POST /auth/validate-token")
  @Headers("Content-Type: application/json")
  UserCredentialResponse getCredential(TokenRequest accessToken);
}
