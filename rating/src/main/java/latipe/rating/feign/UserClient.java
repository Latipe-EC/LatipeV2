package latipe.rating.feign;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import latipe.rating.response.InfoRatingResponse;

public interface UserClient {

    @RequestLine("GET /users/{userId}/internal/info-rating")
    @Headers({"X-API-KEY: {requester}", "Content-Type: application/json"})
    InfoRatingResponse getInfoForRating(@Param("requester") String requester,
        @Param("userId") String userId);
}
