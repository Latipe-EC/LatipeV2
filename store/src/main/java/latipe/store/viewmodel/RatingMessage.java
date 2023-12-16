package latipe.store.viewmodel;

import lombok.Builder;

@Builder
public record RatingMessage(String orderItemId, String productId, String ratingId, Integer rating,
                            String storeId,
                            Integer oldRating,
                            String op) {

}
