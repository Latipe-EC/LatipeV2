package latipe.cart.viewmodel;

import jakarta.validation.constraints.Min;
import latipe.cart.annotations.IsObjectId;

public record CartItemVm(@IsObjectId String productId,
                         @Min(value = 1, message = "quantity must be greater than 0") int quantity,
                         @IsObjectId String productOptionId) {

}