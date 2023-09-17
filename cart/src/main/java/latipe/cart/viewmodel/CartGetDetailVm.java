package latipe.cart.viewmodel;


import latipe.cart.Entity.Cart;

import java.util.List;

public record CartGetDetailVm(String id, String userId, List<CartDetailVm> cartDetails) {
    public static CartGetDetailVm fromModel(Cart cart) {
        return new CartGetDetailVm(
                cart.getId(),
                cart.getUserId(),
                cart.getCartItems().stream().map(CartDetailVm::fromModel).toList());
    }
}
