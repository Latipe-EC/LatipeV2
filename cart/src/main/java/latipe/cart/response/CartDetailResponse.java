package latipe.cart.response;


import latipe.cart.Entity.CartItem;

public record CartDetailResponse(String id, String productId, String productOptionId, int quantity) {
    public static CartDetailResponse fromModel(CartItem cartItem) {
        return new CartDetailResponse(
                cartItem.getId(),
                cartItem.getProductId(),
                cartItem.getProductOptionId(),
                cartItem.getQuantity());
    }
}
