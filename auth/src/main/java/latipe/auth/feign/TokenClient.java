package latipe.auth.feign;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import latipe.auth.request.ForgotPasswordRequest;
import latipe.auth.request.RequestVerifyAccountRequest;
import latipe.auth.request.ResetPasswordRequest;
import latipe.auth.request.VerifyAccountRequest;
import org.springframework.web.bind.annotation.RequestBody;

public interface TokenClient {

  @RequestLine("POST /tokens/finish-verify-account")
  @Headers({"X-API-KEY: {requester}", "Content-Type: application/json"})
  Void verifyAccount(@Param("requester") String requester,
      @RequestBody VerifyAccountRequest request);


  @RequestLine("POST /tokens/forgot-password")
  @Headers({"X-API-KEY: {requester}", "Content-Type: application/json"})
  void forgotPassword(@Param("requester") String requester,
      @RequestBody ForgotPasswordRequest request);

  @RequestLine("POST /tokens/verify-account")
  @Headers({"X-API-KEY: {requester}", "Content-Type: application/json"})
  void requestVerifyAccount(@Param("requester") String requester,
      @RequestBody RequestVerifyAccountRequest request);

  @RequestLine("POST /tokens/reset-password")
  @Headers({"X-API-KEY: {requester}", "Content-Type: application/json"})
  void resetPassword(@Param("requester") String requester,
      @RequestBody ResetPasswordRequest request);


}
