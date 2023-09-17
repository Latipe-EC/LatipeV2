package latipe.cart.viewmodel;


import latipe.cart.Entity.Cart;

public record CartListVm(String id, String userId) {
    public static CartListVm fromModel(Cart cart) {
        return new CartListVm(cart.getId(), cart.getUserId());
    }
}
