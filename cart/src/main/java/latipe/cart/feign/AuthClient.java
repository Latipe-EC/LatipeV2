package latipe.cart.feign;

import feign.Headers;
import feign.RequestLine;
import latipe.cart.request.TokenRequest;
import latipe.cart.response.UserCredentialResponse;

public interface AuthClient {

  @RequestLine("POST /auth/validate-token")
  @Headers("Content-Type: application/json")
  UserCredentialResponse getCredential(TokenRequest accessToken);
}
