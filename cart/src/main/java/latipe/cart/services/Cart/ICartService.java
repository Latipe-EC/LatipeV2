package latipe.cart.services.Cart;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import latipe.cart.request.ProductFeatureRequest;
import latipe.cart.response.UserCredentialResponse;
import latipe.cart.response.CartGetDetailResponse;
import latipe.cart.response.CartItemPutResponse;
import latipe.cart.request.CartItemRequest;
import latipe.cart.response.CartListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ICartService {
    public CompletableFuture<Integer> countNumberItemInCart(String userId);

    public CompletableFuture<CartItemPutResponse> updateCartItems(CartItemRequest cartItemRequest, String cartId,
        UserCredentialResponse userCredential);

    public CompletableFuture<Void> removeCartItemById(String cartId, String cartItemId,
        UserCredentialResponse userCredential);

    public CompletableFuture<CartGetDetailResponse> addToCart(List<CartItemRequest> cartItemRequests,
        UserCredentialResponse userCredential);
    public CompletableFuture<CartGetDetailResponse> getCartDetailByCustomerId(String userId);
    public CompletableFuture<Page<CartListResponse>> getCarts(Pageable pageable);

    public CompletableFuture<CartItemPutResponse> updateCartItems(CartItemRequest cartItemRequest,
        UserCredentialResponse userCredential);

    public CompletableFuture<Void> removeCartItemById(ProductFeatureRequest product,
        UserCredentialResponse userCredential);

    public CompletableFuture<Void> removeCartItemByIdList(String cartId, List<String> cartItemIds,
        UserCredentialResponse userCredential);

    public CompletableFuture<Void> removeCartItemByIdList(List<ProductFeatureRequest> productIdList,
        UserCredentialResponse userCredential);

}
