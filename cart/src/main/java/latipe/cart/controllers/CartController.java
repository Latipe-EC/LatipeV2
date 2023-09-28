package latipe.cart.controllers;

import static latipe.cart.constants.CONSTANTS.ADMIN;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import latipe.cart.annotations.ApiPrefixController;
import latipe.cart.annotations.Authenticate;
import latipe.cart.annotations.RequiresAuthorization;
import latipe.cart.request.CartItemRequest;
import latipe.cart.request.ProductFeatureRequest;
import latipe.cart.response.CartGetDetailResponse;
import latipe.cart.response.CartItemPutResponse;
import latipe.cart.response.CartListResponse;
import latipe.cart.response.UserCredentialResponse;
import latipe.cart.services.Cart.ICartService;
import latipe.cart.viewmodel.ExceptionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestController
@ApiPrefixController("carts")
public class CartController {

  private final ICartService cartService;

  public CartController(ICartService cartService) {
    this.cartService = cartService;
  }

  @RequiresAuthorization(ADMIN)
  @GetMapping("/paginate")
  public CompletableFuture<Page<CartListResponse>> listCarts(Pageable pageable) {
    return cartService.getCarts(pageable);
  }

  @Authenticate
  @RequiresAuthorization(ADMIN)
  @GetMapping("/{userId}")
  public CompletableFuture<CartGetDetailResponse> listCartDetailByCustomerId(
      @PathVariable String userId) {
    return cartService.getCartDetailByCustomerId(userId);
  }

  @Authenticate
  @GetMapping("/my-cart")
  public CompletableFuture<CartGetDetailResponse> getMyCart() {
    UserCredentialResponse userCredential = ((UserCredentialResponse) (((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest()
        .getAttribute("user")));

    return cartService.getCartDetailByCustomerId(userCredential.id());
  }

  @Authenticate
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(path = "/carts/add-cart-items")
  @Operation(summary = "Add product to shopping cart. When no cart exists, this will create a new cart.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Add to cart successfully", content = @Content(schema = @Schema(implementation = CartGetDetailResponse.class))),
      @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
      @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))})
  public CompletableFuture<CartGetDetailResponse> createCart(
      @Valid @RequestBody @NotEmpty List<CartItemRequest> cartItemRequests
  ) {
    UserCredentialResponse userCredential = ((UserCredentialResponse) (((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest()
        .getAttribute("user")));
    return cartService.addToCart(cartItemRequests, userCredential);
  }

  @Authenticate
  @PutMapping("/{cartId}/cart-item")
  public CompletableFuture<CartItemPutResponse> updateCart(
      @Valid @RequestBody CartItemRequest cartItemRequest,
      @PathVariable String cartId
  ) {
    UserCredentialResponse userCredential = ((UserCredentialResponse) (((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest()
        .getAttribute("user")));

    return cartService.updateCartItems(cartItemRequest, cartId, userCredential);
  }

  @Authenticate
  @PutMapping("/cart-item")
  public CompletableFuture<CartItemPutResponse> updateCart(
      @Valid @RequestBody CartItemRequest cartItemRequest
  ) {
    UserCredentialResponse userCredential = ((UserCredentialResponse) (((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest()
        .getAttribute("user")));

    return cartService.updateCartItems(cartItemRequest, userCredential);
  }

  @Authenticate
  @DeleteMapping("/{cartId}/cart-item")
  public CompletableFuture<Void> removeCartItemByProductId(@PathVariable String cartId,
      @RequestParam String cartItemId
  ) {
    UserCredentialResponse userCredential = ((UserCredentialResponse) (((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest()
        .getAttribute("user")));

    return cartService.removeCartItemById(cartId, cartItemId, userCredential);
  }

  @Authenticate
  @DeleteMapping("/{cartId}/cart-item/multi-delete")
  public CompletableFuture<Void> removeCartItemListByProductIdList(
      @PathVariable String cartId,
      @RequestParam List<String> productIds
  ) {
    UserCredentialResponse userCredential = ((UserCredentialResponse) (((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest()
        .getAttribute("user")));

    return cartService.removeCartItemByIdList(cartId, productIds, userCredential);
  }

  @Authenticate
  @DeleteMapping("/cart-item")
  public CompletableFuture<Void> removeCartItemByProductId(
      @Valid @RequestBody ProductFeatureRequest product
  ) {
    UserCredentialResponse userCredential = ((UserCredentialResponse) (((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest()
        .getAttribute("user")));

    return cartService.removeCartItemById(product, userCredential);
  }

  @Authenticate
  @DeleteMapping("/cart-item/multi-delete")
  public CompletableFuture<Void> removeCartItemListByProductIdList(
      @Valid @RequestBody List<ProductFeatureRequest> products
  ) {
    UserCredentialResponse userCredential = ((UserCredentialResponse) (((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest()
        .getAttribute("user")));

    return cartService.removeCartItemByIdList(products, userCredential);
  }

  @Authenticate
  @GetMapping(path = "/count-my-cart-items")
  public CompletableFuture<Integer> getNumberItemInCart() {
    UserCredentialResponse userCredential = ((UserCredentialResponse) (((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest()
        .getAttribute("user")));

    return cartService.countNumberItemInCart(userCredential.id());
  }

}
