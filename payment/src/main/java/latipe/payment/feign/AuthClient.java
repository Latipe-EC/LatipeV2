package latipe.payment.feign;

import feign.Headers;
import feign.RequestLine;
import latipe.payment.request.TokenRequest;
import latipe.payment.response.UserCredentialResponse;

public interface AuthClient {

    @RequestLine("POST /auth/validate-token")
    @Headers("Content-Type: application/json")
    UserCredentialResponse getCredential(TokenRequest accessToken);
}
