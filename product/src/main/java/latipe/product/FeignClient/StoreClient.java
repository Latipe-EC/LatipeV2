package latipe.product.FeignClient;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

public interface StoreClient {

  @RequestLine("GET /stores/validate-store/{userId}")
  @Headers("Authorization: {requester}")
  String getStoreId(@Param("requester") String requester, @Param("userId") String userId);
}
