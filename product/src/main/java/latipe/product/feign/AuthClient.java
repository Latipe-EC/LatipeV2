package latipe.product.feign;

import feign.Headers;
import feign.RequestLine;
import latipe.product.request.TokenRequest;
import latipe.product.response.UserCredentialResponse;

public interface AuthClient {

  @RequestLine("POST /auth/validate-token")
  @Headers("Content-Type: application/json")
  UserCredentialResponse getCredential(TokenRequest accessToken);
}
