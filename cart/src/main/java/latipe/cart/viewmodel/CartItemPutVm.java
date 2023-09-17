package latipe.cart.viewmodel;

import latipe.cart.Entity.CartItem;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public record CartItemPutVm(String cartItemId, String userId, String productId, String productOptionId,
                            Integer quantity, String status) {

    public static CartItemPutVm fromModel(CartItem cartItem, String status) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String customerId = auth.getName();
        return new CartItemPutVm(cartItem.getId(), customerId, cartItem.getProductId(), cartItem.getProductOptionId(), cartItem.getQuantity(), status);
    }
}