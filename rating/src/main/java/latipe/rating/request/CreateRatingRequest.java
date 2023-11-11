package latipe.rating.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRatingRequest(

    @NotBlank(message = "Name must not be blank") String content,
    @Size(min = 1, max = 5, message = "Rating must be between 1 and 5") Integer rating,
    @NotBlank(message = "Store ID must not be blank") String storeId, String productId,
    String detail, String order, String orderItemId) {

}
