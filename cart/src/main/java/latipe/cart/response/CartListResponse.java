package latipe.cart.response;


import latipe.cart.Entity.Cart;

public record CartListResponse(String id, String userId) {
    public static CartListResponse fromModel(Cart cart) {
        return new CartListResponse(cart.getId(), cart.getUserId());
    }
}
