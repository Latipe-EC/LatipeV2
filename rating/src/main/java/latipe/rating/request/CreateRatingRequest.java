package latipe.rating.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import latipe.rating.annotations.IsObjectId;

public record CreateRatingRequest(

    @NotBlank(message = "Name must not be blank") String content,
    @Size(min = 1, max = 5, message = "Rating must be between 1 and 5") Integer rating,
    @IsObjectId String storeId, @IsObjectId String productId, String detail, String orderItemId) {

}
