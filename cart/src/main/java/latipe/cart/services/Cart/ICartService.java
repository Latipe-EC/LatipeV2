package latipe.cart.services.Cart;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import latipe.cart.request.CartItemRequest;
import latipe.cart.request.ProductFeatureRequest;
import latipe.cart.response.CartGetDetailResponse;
import latipe.cart.response.CartItemPutResponse;
import latipe.cart.response.CartListResponse;
import latipe.cart.response.UserCredentialResponse;
import latipe.cart.viewmodel.UpdateCartAfterOrderVm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ICartService {

  CompletableFuture<Integer> countNumberItemInCart(String userId);

  CompletableFuture<CartItemPutResponse> updateCartItems(CartItemRequest cartItemRequest,
      String cartId,
      UserCredentialResponse userCredential);

  CompletableFuture<Void> removeCartItemById(String cartId, String cartItemId,
      UserCredentialResponse userCredential);

  CompletableFuture<Void> removeCartItemAfterOrder(UpdateCartAfterOrderVm updateCartAfterOrderVm);

  CompletableFuture<CartGetDetailResponse> addToCart(List<CartItemRequest> cartItemRequests,
      UserCredentialResponse userCredential);

  CompletableFuture<CartGetDetailResponse> getCartDetailByCustomerId(String userId);

  CompletableFuture<Page<CartListResponse>> getCarts(Pageable pageable);

  CompletableFuture<CartItemPutResponse> updateCartItems(CartItemRequest cartItemRequest,
      UserCredentialResponse userCredential);

  CompletableFuture<Void> removeCartItemById(ProductFeatureRequest product,
      UserCredentialResponse userCredential);

  CompletableFuture<Void> removeCartItemByIdList(String cartId, List<String> cartItemIds,
      UserCredentialResponse userCredential);

  CompletableFuture<Void> removeCartItemByIdList(List<ProductFeatureRequest> productIdList,
      UserCredentialResponse userCredential);

}
