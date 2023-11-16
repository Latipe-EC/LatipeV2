package latipe.cart.request;

import jakarta.validation.constraints.Min;
import latipe.cart.annotations.IsObjectId;

public record UpdateQuantityRequest(
    @IsObjectId
    String id,
    @Min(1)
    int quantity) {

}
