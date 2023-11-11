package latipe.rating.viewmodel;

import lombok.Builder;

@Builder
public record RatingMessage(String orderItemId, String ratingId, String op) {

}
