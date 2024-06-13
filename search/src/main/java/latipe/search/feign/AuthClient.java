package latipe.search.feign;

import feign.Headers;
import feign.RequestLine;
import latipe.search.request.TokenRequest;
import latipe.search.response.UserCredentialResponse;

public interface AuthClient {

    @RequestLine("POST /auth/validate-token")
    @Headers("Content-Type: application/json")
    UserCredentialResponse getCredential(TokenRequest accessToken);
}
