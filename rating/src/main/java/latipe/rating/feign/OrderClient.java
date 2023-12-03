package latipe.rating.feign;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import latipe.rating.response.OrderItemRatingResponse;

public interface OrderClient {

  @RequestLine("GET /orders/internal/rating/:id")
  @Headers({"X-API-KEY: {requester}", "Content-Type: application/json"})
  OrderItemRatingResponse getRating(@Param("requester") String requester, String orderId);
}
