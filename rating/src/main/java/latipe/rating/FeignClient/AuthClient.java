package latipe.rating.FeignClient;

import feign.Headers;
import feign.RequestLine;
import latipe.rating.request.TokenRequest;
import latipe.rating.response.UserCredentialResponse;

public interface AuthClient {

  @RequestLine("POST /auth/validate-token")
  @Headers("Content-Type: application/json")
  UserCredentialResponse getCredential(TokenRequest accessToken);
}
