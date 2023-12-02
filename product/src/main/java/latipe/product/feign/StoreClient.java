package latipe.product.feign;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import java.util.List;
import latipe.product.request.GetProvinceCodesRequest;
import latipe.product.request.MultipleStoreRequest;
import latipe.product.response.ProvinceCodesResponse;
import latipe.product.response.StoreResponse;
import latipe.product.response.StoreSimplifyResponse;
import org.springframework.web.bind.annotation.RequestBody;

public interface StoreClient {

  @RequestLine("GET /stores/validate-store/{userId}")
  @Headers("Authorization: {requester}")
  String getStoreId(@Param("requester") String requester, @Param("userId") String userId);

  @RequestLine("GET /stores/{userId}")
  StoreResponse getDetailStore(@Param("userId") String userId);

  @RequestLine("GET /stores/{userId}")
  @Headers({"X-API-KEY: {requester}", "Content-Type: application/json"})
  List<StoreResponse> getListDetailStore(@Param("userId") String userId);

  @RequestLine("POST /stores/get-province-codes")
  @Headers({"X-API-KEY: {requester}", "Content-Type: application/json"})
  ProvinceCodesResponse getProvinceCodes(@Param("requester") String requester,
      @RequestBody GetProvinceCodesRequest request);

  @RequestLine("POST /stores/multiple-detail-store")
  @Headers({"X-API-KEY: {requester}", "Content-Type: application/json"})
  List<StoreSimplifyResponse> getDetailStores(@Param("requester") String requester,
      @RequestBody MultipleStoreRequest input);

}
