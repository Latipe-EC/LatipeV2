package latipe.cart.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import latipe.cart.annotations.ApiPrefixController;
import latipe.cart.annotations.Authenticate;
import latipe.cart.annotations.RequiresAuthorization;
import latipe.cart.dtos.ProductFeatureDto;
import latipe.cart.dtos.UserCredentialDto;
import latipe.cart.services.Cart.ICartService;
import latipe.cart.viewmodel.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static latipe.cart.constants.CONSTANTS.ADMIN;

@RestController
@ApiPrefixController("carts")
public class CartController {
    private final ICartService cartService;

    public CartController(ICartService cartService) {
        this.cartService = cartService;
    }

    @RequiresAuthorization(ADMIN)
    @GetMapping("/paginate")
    public CompletableFuture<Page<CartListVm>> listCarts(Pageable pageable) {
        return cartService.getCarts(pageable);
    }

    @Authenticate
    @RequiresAuthorization(ADMIN)
    @GetMapping("/{userId}")
    public CompletableFuture<CartGetDetailVm> listCartDetailByCustomerId(@PathVariable String userId) {
        return cartService.getCartDetailByCustomerId(userId);
    }

    @Authenticate
    @GetMapping("/my-cart")
    public CompletableFuture<CartGetDetailVm> getMyCart(@RequestAttribute(value = "user") UserCredentialDto userCredential) {
        return cartService.getCartDetailByCustomerId(userCredential.getId());
    }

    @Authenticate
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "/carts/add-cart-items")
    @Operation(summary = "Add product to shopping cart. When no cart exists, this will create a new cart.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Add to cart successfully", content = @Content(schema = @Schema(implementation = CartGetDetailVm.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))})
    public CompletableFuture<CartGetDetailVm> createCart(@Valid @RequestBody @NotEmpty List<CartItemVm> cartItemVms,
                                                         @RequestAttribute(value = "user") UserCredentialDto userCredential
    ) {
        return cartService.addToCart(cartItemVms, userCredential);
    }

    @Authenticate
    @PutMapping("/{cartId}/cart-item")
    public CompletableFuture<CartItemPutVm> updateCart(@Valid @RequestBody CartItemVm cartItemVm,
                                                       @PathVariable String cartId,
                                                       @RequestAttribute(value = "user") UserCredentialDto userCredential) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return cartService.updateCartItems(cartItemVm, cartId, userCredential);
    }
    @Authenticate
    @PutMapping("/cart-item")
    public CompletableFuture<CartItemPutVm> updateCart(@Valid @RequestBody CartItemVm cartItemVm,
                                                       @RequestAttribute(value = "user") UserCredentialDto userCredential) {
        return cartService.updateCartItems(cartItemVm, userCredential);
    }
    @Authenticate
    @DeleteMapping("/{cartId}/cart-item")
    public CompletableFuture<Void> removeCartItemByProductId(@PathVariable String cartId,
                                                             @RequestParam String cartItemId,
                                                             @RequestAttribute(value = "user") UserCredentialDto userCredential) {
        return cartService.removeCartItemById(cartId, cartItemId, userCredential);
    }
    @Authenticate
    @DeleteMapping("/{cartId}/cart-item/multi-delete")
    public CompletableFuture<Void> removeCartItemListByProductIdList(
            @PathVariable String cartId,
            @RequestParam List<String> productIds,
            @RequestAttribute(value = "user") UserCredentialDto userCredential) {
        return cartService.removeCartItemByIdList(cartId, productIds, userCredential);
    }
    @Authenticate
    @DeleteMapping("/cart-item")
    public CompletableFuture<Void> removeCartItemByProductId(
            @Valid @RequestBody ProductFeatureDto product,
            @RequestAttribute(value = "user") UserCredentialDto userCredential) {
        return cartService.removeCartItemById(product, userCredential);
    }
    @Authenticate
    @DeleteMapping("/cart-item/multi-delete")
    public CompletableFuture<Void> removeCartItemListByProductIdList(
            @Valid @RequestBody List<ProductFeatureDto> products,
            @RequestAttribute(value = "user") UserCredentialDto userCredential) {
        return cartService.removeCartItemByIdList(products, userCredential);
    }
    @Authenticate
    @GetMapping(path = "/count-my-cart-items")
    public CompletableFuture<Integer> getNumberItemInCart(@RequestAttribute(value = "user") UserCredentialDto userCredential) {
        return cartService.countNumberItemInCart(userCredential.getId());
    }

}
