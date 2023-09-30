package latipe.store.FeignClient;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

public interface UserClient {

  @RequestLine("PUT /users/upgrade-to-vendor")
  @Headers("Authorization: {requester}")
  void upgradeVendor(@Param("requester") String requester);
}
