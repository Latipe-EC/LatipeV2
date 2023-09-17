package latipe.cart.viewmodel;

import jakarta.validation.constraints.Min;
import latipe.cart.Entity.CartItem;

public record CartItemVm(String productId, int quantity, String productOptionId) {
    public static CartItemVm fromModel(CartItem cartItem) {
        return new CartItemVm(cartItem.getProductId(), cartItem.getQuantity(), cartItem.getProductOptionId());
    }
}
