package latipe.cart.request;

import latipe.cart.Entity.CartItem;

public record CartItemRequest(String productId, int quantity, String productOptionId) {
    public static CartItemRequest fromModel(CartItem cartItem) {
        return new CartItemRequest(cartItem.getProductId(), cartItem.getQuantity(), cartItem.getProductOptionId());
    }
}
