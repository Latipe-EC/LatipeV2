package latipe.media.feign;

import feign.Headers;
import feign.RequestLine;
import latipe.media.request.TokenRequest;
import latipe.media.response.UserCredentialResponse;

public interface AuthClient {

  @RequestLine("POST /auth/validate-token")
  @Headers("Content-Type: application/json")
  UserCredentialResponse getCredential(TokenRequest accessToken);
}
