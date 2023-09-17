package latipe.cart.services.Cart;

import latipe.cart.dtos.UserCredentialDto;
import latipe.cart.viewmodel.CartGetDetailVm;
import latipe.cart.viewmodel.CartItemPutVm;
import latipe.cart.viewmodel.CartItemVm;
import latipe.cart.viewmodel.CartListVm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ICartService {
    public CompletableFuture<Integer> countNumberItemInCart(String userId);

    public CompletableFuture<CartItemPutVm> updateCartItems(CartItemVm cartItemVm, String cartId, UserCredentialDto userCredential);

    public CompletableFuture<Void> removeCartItemByIdList(String cartId, List<String> productIdList, UserCredentialDto userCredential);

    public CompletableFuture<Void> removeCartItemById(String cartId, String cartItemId, UserCredentialDto userCredential);

    public CompletableFuture<CartGetDetailVm> addToCart(List<CartItemVm> cartItemVms, UserCredentialDto userCredential);

    public CompletableFuture<CartGetDetailVm> getCartDetailByCustomerId(String userId);

    public CompletableFuture<Page<CartListVm>> getCarts(Pageable pageable);

    public CompletableFuture<CartItemPutVm> updateCartItems(CartItemVm cartItemVm, UserCredentialDto userCredential);

    public CompletableFuture<Void> removeCartItemByIdList(List<String> productIdList, UserCredentialDto userCredential);

    public CompletableFuture<Void> removeCartItemById(String cartItemId, UserCredentialDto userCredential);


}
