package latipe.rating.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateRatingRequest(
    @NotBlank(message = "Name must not be blank")
    String content,
    @Min(1)
    @Max(5)
    Integer rating
) {

}
