package latipe.auth.feign;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import latipe.auth.request.VerifyAccountRequest;
import org.springframework.web.bind.annotation.RequestBody;

public interface TokenClient {

  @RequestLine("POST /tokens/verify-account")
  @Headers({"X-API-KEY: {requester}", "Content-Type: application/json"})
  Void verifyAccount(@Param("requester") String requester,
      @RequestBody VerifyAccountRequest request);

}
