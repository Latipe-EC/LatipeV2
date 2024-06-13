package latipe.store.feign;

import feign.Headers;
import feign.RequestLine;
import latipe.store.request.TokenRequest;
import latipe.store.response.UserCredentialResponse;

public interface AuthClient {

    @RequestLine("POST /auth/validate-token")
    @Headers("Content-Type: application/json")
    UserCredentialResponse getCredential(TokenRequest accessToken);
}
