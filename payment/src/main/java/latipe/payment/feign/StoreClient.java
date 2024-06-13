package latipe.payment.feign;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import latipe.payment.request.CheckBalanceStoreRequest;
import latipe.payment.request.UpdateBalanceStoreRequest;
import org.springframework.web.bind.annotation.RequestBody;

public interface StoreClient {

    @RequestLine("POST /stores/check-balance")
    @Headers({"X-API-KEY: {requester}", "Content-Type: application/json"})
    void checkBalance(@Param("requester") String requester,
        @RequestBody CheckBalanceStoreRequest request);


    @RequestLine("PATCH /stores/balance")
    @Headers({"X-API-KEY: {requester}", "Content-Type: application/json"})
    void updateBalance(@Param("requester") String requester,
        @RequestBody UpdateBalanceStoreRequest request);
}
