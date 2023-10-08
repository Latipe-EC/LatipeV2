package latipe.cart.services.Cart;

import static latipe.cart.constants.CONSTANTS.ADMIN;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import latipe.cart.Entity.Cart;
import latipe.cart.Entity.CartItem;
import latipe.cart.exceptions.BadRequestException;
import latipe.cart.exceptions.ForbiddenException;
import latipe.cart.exceptions.NotFoundException;
import latipe.cart.repositories.ICartRepository;
import latipe.cart.request.CartItemRequest;
import latipe.cart.request.ProductFeatureRequest;
import latipe.cart.response.CartGetDetailResponse;
import latipe.cart.response.CartItemPutResponse;
import latipe.cart.response.CartListResponse;
import latipe.cart.response.ProductThumbnailResponse;
import latipe.cart.response.UserCredentialResponse;
import latipe.cart.services.Product.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
  public CompletableFuture<Page<CartListResponse>> getCarts(Pageable pageable) {
    return CompletableFuture.supplyAsync(
        () -> cartRepository.findAll(pageable).map(CartListResponse::fromModel)
    );
  }

  @Async
  @Override
  public CompletableFuture<CartGetDetailResponse> addToCart(List<CartItemRequest> cartItemRequests,
      UserCredentialResponse userCredential) {
    return CompletableFuture.supplyAsync(
        () -> {
          List<ProductFeatureRequest> productIds = cartItemRequests.stream()
              .map(x -> new ProductFeatureRequest(x.productId(), x.productOptionId())).toList();
          String userId = userCredential.id();
          // Call API to check all products will be added to cart are existed
          List<ProductThumbnailResponse> productThumbnailResponseList = null;
          try {
            productThumbnailResponseList = productService.getProducts(productIds).get();
          } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
          }
          if (productThumbnailResponseList.size() != productIds.size()) {
            throw new NotFoundException("Not found product");
          }
          Cart cart = cartRepository.findByUserId(userId).orElse(null);
          Set<CartItem> existedCartItems = new HashSet<>();
          if (cart == null) {
            cart = new Cart();
            cart.setUserId(userId);
            cart.setCartItems(existedCartItems);
            cart.setCreatedDate(new Date());
          } else {
            existedCartItems = cart.getCartItems();
          }

          for (CartItemRequest cartItemRequest : cartItemRequests) {
            CartItem cartItem = getCartItemByProductIdAndOptionId(existedCartItems,
                cartItemRequest.productId(), cartItemRequest.productOptionId());
            if (cartItem.getId() != null) {
              cartItem.setQuantity(cartItem.getQuantity() + cartItemRequest.quantity());
            } else {
              cartItem.setProductId(cartItemRequest.productId());
              cartItem.setQuantity(cartItemRequest.quantity());
              cartItem.setProductOptionId(cartItemRequest.productOptionId());
              cart.getCartItems().add(cartItem);
            }
          }
          cart = cartRepository.save(cart);
          return CartGetDetailResponse.fromModel(cart);
        }
    );

  }

  @Async
  @Override
  public CompletableFuture<CartGetDetailResponse> getCartDetailByCustomerId(String userId) {
    return CompletableFuture.supplyAsync(
        () -> cartRepository.findByUserId(userId).stream().findFirst().map(
            CartGetDetailResponse::fromModel).orElse(null)
    );
  }

  @Async
  @Override
  public CompletableFuture<Void> removeCartItemById(String cartId, String cartItemId,
      UserCredentialResponse userCredential) {
    Cart cart = cartRepository.findById(cartId).orElseThrow(
        () -> new NotFoundException("Not found cart with %s cartId".formatted(cartId))
    );
    return removeListCartItemsById(List.of(
        cartItemId
    ), cart, userCredential);
  }

  @Async
  @Override
  public CompletableFuture<Void> removeCartItemById(ProductFeatureRequest product,
      UserCredentialResponse userCredential) {
    Cart cart = cartRepository.findByUserId(userCredential.id()).orElseThrow(
        () -> new NotFoundException("Not found user with %s userId".formatted(userCredential.id()))
    );
    return removeListCartItemsByOptionId(List.of(
        product
    ), userCredential, cart);
  }


  @Async
  @Override
  @Transactional
  public CompletableFuture<Void> removeCartItemByIdList(String cartId, List<String> cartItemIds,
      UserCredentialResponse userCredential) {
    Cart cart = cartRepository.findById(cartId).orElseThrow(
        () -> new NotFoundException("Not found cart with %s cartId".formatted(cartId))
    );
    return removeListCartItemsById(cartItemIds, cart, userCredential);
  }

  @Async
  @Override
  @Transactional
  public CompletableFuture<Void> removeCartItemByIdList(List<ProductFeatureRequest> productIdList,
      UserCredentialResponse userCredential) {
    Cart cart = cartRepository.findByUserId(userCredential.id()).orElseThrow(
        () -> new NotFoundException("Not found user with %s userId".formatted(userCredential.id()))
    );
    return removeListCartItemsByOptionId(productIdList, userCredential, cart);
  }

  @Async
  @Override
  public CompletableFuture<CartItemPutResponse> updateCartItems(CartItemRequest cartItemRequest,
      String cartId, UserCredentialResponse userCredential) {
    return CompletableFuture.supplyAsync(
        () -> {
          Cart cart = cartRepository.findById(cartId).orElseThrow(
              () -> new NotFoundException("Not found cart with %s cartId".formatted(cartId))
          );
          return update(cart, userCredential.id(), cartItemRequest);
        }
    );
  }

  @Override
  @Async
  public CompletableFuture<CartItemPutResponse> updateCartItems(CartItemRequest cartItemRequest,
      UserCredentialResponse userCredential) {
    return CompletableFuture.supplyAsync(
        () -> {
          Cart cart = cartRepository.findByUserId(userCredential.id()).orElseThrow(
              () -> new NotFoundException(
                  "Not found user with %s userId".formatted(userCredential.id()))
          );
          return update(cart, userCredential.id(), cartItemRequest);
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
        item.getProductOptionId().equals(optionId));
    if (!itemExist) {
      throw new NotFoundException("NOT FOUND ITEM IN CART");
    }
  }

  private CartItem getCartItemByProductIdAndOptionId(Set<CartItem> cartItems, String productId,
      String optionId) {
    for (CartItem cartItem : cartItems) {
      if (cartItem.getProductId().equals(productId) && cartItem.getProductOptionId()
          .equals(optionId)) {
        return cartItem;
      }
    }
    return new CartItem();
  }

  private CartItemPutResponse update(Cart cart, String userId, CartItemRequest cartItemRequest) {
    if (!cart.getUserId().equals(userId)) {
      throw new ForbiddenException("You are not allowed to update this cart");
    }
    int newQuantity = cartItemRequest.quantity();
    CartItem cartItem = cart.getCartItems().stream()
        .filter(item -> item.getProductId().equals(cartItemRequest.productId())
            && item.getProductOptionId().equals(cartItemRequest.productOptionId()))
        .findFirst().orElseThrow(() -> new NotFoundException("Not found cart item"));
    cartItem.setQuantity(newQuantity);
    if (newQuantity == 0) {
      cart.getCartItems().remove(cartItem);
      cartRepository.save(cart);
      return CartItemPutResponse.fromModel(cartItem, String.format("PRODUCT %s", "DELETED"));
    } else {
      cartItem.setQuantity(newQuantity);
      cart = cartRepository.save(cart);
      return CartItemPutResponse.fromModel(cartItem, String.format("PRODUCT %s", "UPDATED"));
    }
  }

  // remove by cart item id
  private CompletableFuture<Void> removeListCartItemsById(List<String> cartItmesIdList, Cart cart,
      UserCredentialResponse userCredential) {
    if (!cart.getUserId().equals(userCredential.id()) || !userCredential.role().equals(ADMIN)) {
      throw new ForbiddenException("You are not allowed to delete this cart");
    }
    cartItmesIdList.forEach(id -> validateCart(cart, id));
    cart.getCartItems().removeIf(cartItem -> cartItmesIdList.contains(cartItem.getId()));
    cartRepository.save(cart);
    return CompletableFuture.completedFuture(null);
  }

  // remove by product id
  private CompletableFuture<Void> removeListCartItemsByOptionId(
      List<ProductFeatureRequest> productIdList, UserCredentialResponse userCredential, Cart cart) {
    if (!cart.getUserId().equals(userCredential.id()) || !userCredential.role().equals(ADMIN)) {
      throw new ForbiddenException("You are not allowed to delete this cart");
    }
    productIdList.forEach(product -> validateCart(cart, product.productId(), product.optionId()));
    List<String> optionIds = productIdList.stream().map(ProductFeatureRequest::optionId).toList();
    cart.getCartItems().removeIf(cartItem -> optionIds.contains(cartItem.getProductOptionId()));
    cartRepository.save(cart);
    return CompletableFuture.completedFuture(null);
  }

}
