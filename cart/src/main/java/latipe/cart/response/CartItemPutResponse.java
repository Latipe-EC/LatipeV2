package latipe.cart.response;

import latipe.cart.Entity.CartItem;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public record CartItemPutResponse(String cartItemId, String userId, String productId,
                                  String productOptionId,
                                  Integer quantity, String status) {

    public static CartItemPutResponse fromModel(CartItem cartItem, String status) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String customerId = auth.getName();
        return new CartItemPutResponse(cartItem.getId(), customerId, cartItem.getProductId(),
            cartItem.getProductOptionId(), cartItem.getQuantity(), status);
    }
}