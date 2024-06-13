package latipe.auth.feign;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import latipe.auth.request.RegisterRequest;
import latipe.auth.response.UserResponse;
import org.springframework.web.bind.annotation.RequestBody;

public interface UserClient {

    @RequestLine("POST /users/register")
    @Headers({"X-API-KEY: {requester}", "Content-Type: application/json"})
    UserResponse register(@Param("requester") String requester,
        @RequestBody RegisterRequest request);

}
