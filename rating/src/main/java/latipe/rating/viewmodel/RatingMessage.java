package latipe.rating.viewmodel;

import lombok.Builder;

@Builder
public record RatingMessage(String orderItemId, String productId, String ratingId, Integer rating,
                            String op) {

}
