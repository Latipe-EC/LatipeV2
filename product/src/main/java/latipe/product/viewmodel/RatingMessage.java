package latipe.product.viewmodel;

import lombok.Builder;

@Builder
public record RatingMessage(String orderItemId, String productId, String ratingId, Integer rating,
                            Integer oldRating,
                            String op) {

}
