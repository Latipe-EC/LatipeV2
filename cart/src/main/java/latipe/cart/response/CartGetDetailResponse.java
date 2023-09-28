package latipe.cart.response;


import java.util.List;
import latipe.cart.Entity.Cart;

public record CartGetDetailResponse(String id, String userId,
                                    List<CartDetailResponse> cartDetails) {

  public static CartGetDetailResponse fromModel(Cart cart) {
    return new CartGetDetailResponse(
        cart.getId(),
        cart.getUserId(),
        cart.getCartItems().stream().map(CartDetailResponse::fromModel).toList());
  }
}
