package latipe.cart.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record UpdateQuantityRequest(
    @NotBlank(message = "can-not-be-blank")
    String id,
    @Min(1)
    int quantity) {

}
