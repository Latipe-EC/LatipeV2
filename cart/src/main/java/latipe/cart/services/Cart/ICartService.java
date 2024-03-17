package latipe.cart.services.Cart;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import latipe.cart.dtos.PagedResultDto;
import latipe.cart.request.UpdateQuantityRequest;
import latipe.cart.response.CartGetDetailResponse;
import latipe.cart.response.DeleteCartItemRequest;
import latipe.cart.viewmodel.CartItemVm;
import latipe.cart.viewmodel.UpdateCartAfterOrderVm;

public interface ICartService {

  CompletableFuture<Void> removeCartItemAfterOrder(UpdateCartAfterOrderVm updateCartAfterOrderVm);

  CompletableFuture<Void> updateQuantity(UpdateQuantityRequest input, HttpServletRequest request);

  CompletableFuture<CartGetDetailResponse> addToCart(CartItemVm cartItemRequest,
      HttpServletRequest request);

  CompletableFuture<PagedResultDto<CartGetDetailResponse>> getMyCart(long skip,
      int size, HttpServletRequest request);

  CompletableFuture<Void> deleteCartItem(DeleteCartItemRequest input, HttpServletRequest request);

  CompletableFuture<List<CartGetDetailResponse>> getListCart(List<String> cartIds,
      HttpServletRequest request);

}
