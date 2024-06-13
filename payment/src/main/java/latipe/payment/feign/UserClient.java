package latipe.payment.feign;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import latipe.payment.request.CancelOrderRequest;
import latipe.payment.request.CheckBalanceRequest;
import org.springframework.web.bind.annotation.RequestBody;

public interface UserClient {

    @RequestLine("POST /users/check-balance")
    @Headers({"X-API-KEY: {requester}", "Content-Type: application/json"})
    void checkBalance(@Param("requester") String requester,
        @RequestBody CheckBalanceRequest request);


    @RequestLine("POST /users/cancel-order")
    @Headers({"X-API-KEY: {requester}", "Content-Type: application/json"})
    void cancelOrder(@Param("requester") String requester,
        @RequestBody CancelOrderRequest request);

}
