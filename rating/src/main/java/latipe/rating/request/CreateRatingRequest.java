package latipe.rating.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import latipe.rating.annotations.IsObjectId;

public record CreateRatingRequest(

    @NotBlank(message = "Name must not be blank") String content,
    @Min(1)
    @Max(5)
    Integer rating,
    @IsObjectId String storeId, @IsObjectId String productId, String orderItemId) {

}
