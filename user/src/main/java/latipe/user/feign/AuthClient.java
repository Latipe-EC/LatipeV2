package latipe.user.feign;

import feign.Headers;
import feign.RequestLine;
import latipe.user.request.TokenRequest;
import latipe.user.response.UserCredentialResponse;

public interface AuthClient {

    @RequestLine("POST /auth/validate-token")
    @Headers("Content-Type: application/json")
    UserCredentialResponse getCredential(TokenRequest accessToken);
}
