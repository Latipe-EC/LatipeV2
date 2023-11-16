package latipe.cart.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import latipe.cart.viewmodel.CartItemVm;

public record CartItemRequest(
    @NotEmpty(message = "cartItems can not be empty")
    List<@Valid CartItemVm> cartItems
) {

}
