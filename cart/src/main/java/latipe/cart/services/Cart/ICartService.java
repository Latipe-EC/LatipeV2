package latipe.cart.services.Cart;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import latipe.cart.dtos.PagedResultDto;
import latipe.cart.request.CartItemRequest;
import latipe.cart.request.UpdateQuantityRequest;
import latipe.cart.response.CartGetDetailResponse;
import latipe.cart.response.DeleteCartItemRequest;
import latipe.cart.response.UserCredentialResponse;
import latipe.cart.viewmodel.UpdateCartAfterOrderVm;

public interface ICartService {


  CompletableFuture<Void> removeCartItemAfterOrder(UpdateCartAfterOrderVm updateCartAfterOrderVm);

  CompletableFuture<Void> updateQuantity(String userId, UpdateQuantityRequest request);

  CompletableFuture<List<CartGetDetailResponse>> addToCart(CartItemRequest cartItemRequest,
      UserCredentialResponse userCredential);

  CompletableFuture<PagedResultDto<CartGetDetailResponse>> getMyCart(String userId, long skip,
      int size);

  CompletableFuture<Void> deleteCartItem(String userId, DeleteCartItemRequest request);


}
