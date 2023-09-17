package latipe.cart.services.Cart;

import latipe.cart.Entity.Cart;
import latipe.cart.Entity.CartItem;
import latipe.cart.dtos.UserCredentialDto;
import latipe.cart.exceptions.BadRequestException;
import latipe.cart.exceptions.ForbiddenException;
import latipe.cart.exceptions.NotFoundException;
import latipe.cart.repositories.ICartRepository;
import latipe.cart.services.Product.ProductService;
import latipe.cart.viewmodel.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static latipe.cart.constants.CONSTANTS.ADMIN;

@Service
public class CartService implements ICartService {
    private final ICartRepository cartRepository;
    private final ProductService productService;
    public CartService(ICartRepository cartRepository, ProductService productService) {
        this.cartRepository = cartRepository;
        this.productService = productService;
    }
    @Async
    @Override
    public CompletableFuture<Page<CartListVm>> getCarts(Pageable pageable) {
        return CompletableFuture.supplyAsync(
                () -> cartRepository.findAll(pageable).map(CartListVm::fromModel)
        );
    }

    @Override
    public CompletableFuture<CartItemPutVm> updateCartItems(CartItemVm cartItemVm, UserCredentialDto userCredential) {
        return null;
    }

    @Override
    public CompletableFuture<Void> removeCartItemByIdList(List<String> productIdList, UserCredentialDto userCredential) {
        return null;
    }

    @Override
    public CompletableFuture<Void> removeCartItemById(String cartItemId, UserCredentialDto userCredential) {
        return null;
    }

    @Async
    @Override
    public CompletableFuture<CartGetDetailVm> getCartDetailByCustomerId(String userId) {
        return CompletableFuture.supplyAsync(
                () -> cartRepository.findByUserId(userId).stream().findFirst().map(CartGetDetailVm::fromModel).orElse(null)
        );
    }

    @Async
    @Override
    public CompletableFuture<CartGetDetailVm> addToCart(List<CartItemVm> cartItemVms, UserCredentialDto userCredential) {
        return CompletableFuture.supplyAsync(
                () -> {
                    List<String> productIds = cartItemVms.stream().map(CartItemVm::productId).toList();
                    String userId = userCredential.getId();
                    // Call API to check all products will be added to cart are existed
                    List<ProductThumbnailVm> productThumbnailVmList = null;
                    try {
                        productThumbnailVmList = productService.getProducts(productIds).get();
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                    if (productThumbnailVmList.size() != productIds.size()) {
                        throw new NotFoundException("Not found product");
                    }
                    Cart cart = cartRepository.findByUserId(userId).orElse(null);
                    Set<CartItem> existedCartItems = new HashSet<>();
                    if (cart == null) {
                        cart = Cart.builder()
                                .userId(userId)
                                .cartItems(existedCartItems)
                                .build();
                        cart.setCreatedDate(new Date());
                    } else {
                        existedCartItems = cart.getCartItems();
                    }

                    for (CartItemVm cartItemVm : cartItemVms) {
                        CartItem cartItem = getCartItemByProductIdAndOptionId(existedCartItems, cartItemVm.productId(), cartItemVm.productOptionId());
                        if (cartItem.getId() != null) {
                            cartItem.setQuantity(cartItem.getQuantity() + cartItemVm.quantity());
                        } else {
                            cartItem.setProductId(cartItemVm.productId());
                            cartItem.setQuantity(cartItemVm.quantity());
                            cartItem.setProductOptionId(cartItemVm.productOptionId());
                            cart.getCartItems().add(cartItem);
                        }
                    }
                    cart = cartRepository.save(cart);
                    return CartGetDetailVm.fromModel(cart);
                }
        );

    }

    @Async
    @Override
    public CompletableFuture<Void> removeCartItemById(String cartId, String cartItemId, UserCredentialDto userCredential) {
        Cart cart = cartRepository.findById(cartId).orElseThrow(
                () -> new NotFoundException("Not found cart with %s cartId".formatted(cartId))
        );
        if (!cart.getUserId().equals(userCredential.getId())) {
            throw new ForbiddenException("You are not allowed to delete this cart");
        }
        validateCart(cart, cartItemId);
        cart.getCartItems().removeIf(cartItem -> cartItem.getId().equals(cartItemId));
        cartRepository.save(cart);
        return CompletableFuture.completedFuture(null);
    }

    @Async
    @Override
    @Transactional
    public CompletableFuture<Void> removeCartItemByIdList(String cartId, List<String> productIdList, UserCredentialDto userCredential) {
        Cart cart = cartRepository.findById(cartId).orElseThrow(
                () -> new NotFoundException("Not found cart with %s cartId".formatted(cartId))
        );
        if (!cart.getUserId().equals(userCredential.getId()) || !userCredential.getRole().equals(ADMIN)) {
            throw new ForbiddenException("You are not allowed to delete this cart");
        }
        productIdList.forEach(id -> validateCart(cart, id));
        cart.getCartItems().removeIf(cartItem -> productIdList.contains(cartItem.getId()));
        cartRepository.save(cart);
        return CompletableFuture.completedFuture(null);
    }

    @Async
    @Override
    public CompletableFuture<CartItemPutVm> updateCartItems(CartItemVm cartItemVm, String cartId, UserCredentialDto userCredential) {
        return CompletableFuture.supplyAsync(
                () -> {
                    Cart cart = cartRepository.findById(cartId).orElseThrow(
                            () -> new NotFoundException("Not found cart with %s cartId".formatted(cartId))
                    );
                    if (!cart.getUserId().equals(userCredential.getId())) {
                        throw new ForbiddenException("You are not allowed to update this cart");
                    }
                    int newQuantity = cartItemVm.quantity();
                    CartItem cartItem = cart.getCartItems().stream()
                            .filter(item -> item.getProductId().equals(cartItemVm.productId())
                                    && item.getProductOptionId().equals(cartItemVm.productOptionId()))
                            .findFirst().orElseThrow(() -> new NotFoundException("Not found cart item"));
                    cartItem.setQuantity(newQuantity);
                    if (newQuantity == 0) {
                        cart.getCartItems().remove(cartItem);
                        cartRepository.save(cart);
                        return CartItemPutVm.fromModel(cartItem, String.format("PRODUCT %s", "DELETED"));
                    } else {
                        cartItem.setQuantity(newQuantity);
                        cart = cartRepository.save(cart);
                        return CartItemPutVm.fromModel(cartItem, String.format("PRODUCT %s", "UPDATED"));
                    }
                }
        );
    }

    @Async
    @Override
    public CompletableFuture<Integer> countNumberItemInCart(String userId) {
        return CompletableFuture.supplyAsync(
                () -> {
                    Optional<Cart> cartOp = cartRepository.findByUserId(userId);
                    return cartOp.map(cart -> cart.getCartItems().size()).orElse(0);
                }
        );
    }

    private void validateCart(Cart cart, String cartItemId) {
        if (cart.getCartItems().isEmpty()) {
            throw new BadRequestException("NOT EXISTING ITEM IN CART");
        }
        Set<CartItem> cartDetailListVm = cart.getCartItems();
        boolean itemExist = cartDetailListVm.stream().anyMatch(item -> item.getId().equals(cartItemId));
        if (!itemExist) {
            throw new NotFoundException("NOT FOUND ITEM IN CART");
        }
    }

    private void validateCart(Cart cart, String productId, String optionId) {
        if (cart.getCartItems().isEmpty()) {
            throw new BadRequestException("NOT EXISTING ITEM IN CART");
        }
        Set<CartItem> cartDetailListVm = cart.getCartItems();
        boolean itemExist = cartDetailListVm.stream().anyMatch(item ->
                item.getProductOptionId().equals(productId)
                        && item.getProductOptionId().equals(optionId));
        if (!itemExist) {
            throw new NotFoundException("NOT FOUND ITEM IN CART");
        }
    }

    private CartItem getCartItemByProductIdAndOptionId(Set<CartItem> cartItems, String productId, String optionId) {
        for (CartItem cartItem : cartItems) {
            if (cartItem.getProductId().equals(productId) && cartItem.getProductOptionId().equals(optionId))
                return cartItem;
        }
        return new CartItem();
    }

}
