package latipe.cart.viewmodel;


import latipe.cart.Entity.CartItem;

public record CartDetailVm(String id, String productId,String productOptionId, int quantity) {
    public static CartDetailVm fromModel(CartItem cartItem) {
        return new CartDetailVm(
                cartItem.getId(),
                cartItem.getProductId(),
                cartItem.getProductOptionId(),
                cartItem.getQuantity());
    }
}
