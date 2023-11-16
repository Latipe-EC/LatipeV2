package latipe.cart.request;

import jakarta.validation.constraints.Size;
import java.util.List;
import latipe.cart.viewmodel.CartItemVm;

public record CartItemRequest(List<@Size(min = 1) CartItemVm> cartItems) {

}
