package latipe.rating.response;

import java.util.Date;
import lombok.Builder;

@Builder
public record RatingResponse(String id, String content, Integer rating, String userId,
                             String userName, String productId, String storeId, String detail,
                             UserResponse user, Date createdDate,
                             Boolean isChange

) {

}
