package latipe.cart.services.Cart;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import latipe.cart.Entity.Cart;
import latipe.cart.dtos.PagedResultDto;
import latipe.cart.dtos.Pagination;
import latipe.cart.exceptions.NotFoundException;
import latipe.cart.repositories.ICartRepository;
import latipe.cart.request.CartItemRequest;
import latipe.cart.request.ProductFeatureRequest;
import latipe.cart.request.UpdateQuantityRequest;
import latipe.cart.response.CartGetDetailResponse;
import latipe.cart.response.DeleteCartItemRequest;
import latipe.cart.response.ProductThumbnailResponse;
import latipe.cart.response.UserCredentialResponse;
import latipe.cart.services.Product.ProductService;
import latipe.cart.viewmodel.CartItemVm;
import latipe.cart.viewmodel.RemoveCartFromOrder;
import latipe.cart.viewmodel.UpdateCartAfterOrderVm;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartService implements ICartService {

  private final ICartRepository cartRepository;
  private final ProductService productService;

  @Async
  @Override
  public CompletableFuture<List<CartGetDetailResponse>> addToCart(CartItemRequest cartItemRequest,
      UserCredentialResponse userCredential) {
    return CompletableFuture.supplyAsync(() -> {
      List<ProductFeatureRequest> productIds = cartItemRequest.cartItems().stream()
          .map(x -> new ProductFeatureRequest(x.productId(), x.productOptionId())).toList();

      String userId = userCredential.id();
      // Call API to check all products will be added to cart are existed
      var productThumbnailResponseList = productService.getProducts(productIds);
      if (productThumbnailResponseList.size() != productIds.size()) {
        throw new NotFoundException("Not found product");
      }

      List<Cart> carts = new ArrayList<>();
      for (CartItemVm cartItemVm : cartItemRequest.cartItems()) {
        Cart cart = cartRepository.findByUserIdAndProductOptionIdAndProductId(userId,
            cartItemVm.productOptionId(), cartItemVm.productId()).orElse(null);
        if (cart != null) {
          cart.setQuantity(cart.getQuantity() + cartItemVm.quantity());
        } else {
          cart = new Cart();
          cart.setUserId(userId);
          cart.setProductId(cartItemVm.productId());
          cart.setProductOptionId(cartItemVm.productOptionId());
          cart.setQuantity(cartItemVm.quantity());
        }
        carts.add(cart);
      }
      carts = cartRepository.saveAll(carts);

      return carts.stream().map(x -> {
        var productDetail = productThumbnailResponseList.stream()
            .filter(y -> y.id().equals(x.getProductId())).findFirst().orElseThrow();
        return CartGetDetailResponse.fromModel(x, productDetail);
      }).toList();
    });

  }

  @Async
  @Override
  public CompletableFuture<PagedResultDto<CartGetDetailResponse>> getMyCart(String userId,
      long skip, int size) {
    return CompletableFuture.supplyAsync(() -> {
      var carts = cartRepository.findMyCart(userId, skip, size);
      var count = cartRepository.countByUserId(userId);

      var productIds = carts.stream()
          .map(x -> new ProductFeatureRequest(x.getProductId(), x.getProductOptionId())).toList();

      // Call API to check all products will be added to cart are existed
      if (productIds.isEmpty()) {
        return PagedResultDto.create(Pagination.create(count, skip, size), new ArrayList<>());
      }

      var productThumbnailResponseList = productService.getProducts(productIds);;

      if (productThumbnailResponseList.size() != productIds.size()) {
        throw new NotFoundException("Not found product");
      }

      var data = carts.stream().map(x -> {
        var productDetail = productThumbnailResponseList.stream()
            .filter(y -> y.id().equals(x.getProductId())).findFirst().orElseThrow();
        return CartGetDetailResponse.fromModel(x, productDetail);
      }).toList();

      return PagedResultDto.create(Pagination.create(count, skip, size), data);
    });
  }


  @Async
  @Override
  public CompletableFuture<Void> updateQuantity(String userId, UpdateQuantityRequest request) {
    return CompletableFuture.supplyAsync(() -> {
      var cart = cartRepository.findById(request.id()).orElseThrow(
          () -> new NotFoundException("Not found cart with %s cartId".formatted(request.id())));
      cart.setQuantity(request.quantity());
      cartRepository.save(cart);
      return null;
    });
  }

  @Async
  @Override
  public CompletableFuture<Void> deleteCartItem(String userId, DeleteCartItemRequest request) {
    return CompletableFuture.supplyAsync(() -> {
      Set<String> cartIds = new HashSet<>(request.ids());
      var carts = cartRepository.findAllByIdAndUserId(cartIds, userId);

      if (carts.size() != cartIds.size()) {
        throw new NotFoundException("Not found cart");
      }

      cartRepository.deleteAll(carts);
      return null;
    });
  }


  @Async
  @Override
  public CompletableFuture<Void> removeCartItemAfterOrder(
      UpdateCartAfterOrderVm updateCartAfterOrderVm) {
    return CompletableFuture.supplyAsync(() -> {
      var ids = updateCartAfterOrderVm.cartIdVmList().stream().map(RemoveCartFromOrder::cart_id)
          .toList();
      var carts = cartRepository.findAllById(ids);
      cartRepository.deleteAll(carts);
      return null;
    });
  }


}
