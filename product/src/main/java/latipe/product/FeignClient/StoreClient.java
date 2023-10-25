package latipe.product.FeignClient;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import java.util.List;
import latipe.product.request.GetProvinceCodesRequest;
import latipe.product.response.ProvinceCodesResponse;
import org.springframework.web.bind.annotation.RequestBody;

public interface StoreClient {

  @RequestLine("GET /stores/validate-store/{userId}")
  @Headers("Authorization: {requester}")
  String getStoreId(@Param("requester") String requester, @Param("userId") String userId);

  @RequestLine("POST /stores/get-province-codes")
  @Headers({"X-API-KEY: {requester}", "Content-Type: application/json"})
  ProvinceCodesResponse getProvinceCodes(@Param("requester") String requester,
      @RequestBody GetProvinceCodesRequest request);


}
