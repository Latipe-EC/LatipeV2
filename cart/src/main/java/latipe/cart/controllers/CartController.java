package latipe.cart.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import latipe.cart.annotations.ApiPrefixController;
import latipe.cart.annotations.Authenticate;
import latipe.cart.dtos.PagedResultDto;
import latipe.cart.exceptions.BadRequestException;
import latipe.cart.exceptions.ForbiddenException;
import latipe.cart.exceptions.NotFoundException;
import latipe.cart.exceptions.UnauthorizedException;
import latipe.cart.request.UpdateQuantityRequest;
import latipe.cart.response.CartGetDetailResponse;
import latipe.cart.response.DeleteCartItemRequest;
import latipe.cart.services.Cart.ICartService;
import latipe.cart.viewmodel.CartItemVm;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ApiPrefixController("carts")
@Validated
public class CartController {

    private final ICartService cartService;

    public CartController(ICartService cartService) {
        this.cartService = cartService;
    }

    @Authenticate
    @GetMapping("/my-cart")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "get cart successfully", content = @Content(schema = @Schema())),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = UnauthorizedException.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ForbiddenException.class))),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = NotFoundException.class))),
        @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = BadRequestException.class)))})
    @Operation(summary = "Get my cart.")
    public CompletableFuture<PagedResultDto<CartGetDetailResponse>> getMyCart(
        @RequestParam(defaultValue = "0") long skip, @RequestParam(defaultValue = "5") int limit,
        HttpServletRequest request) {

        return cartService.getMyCart(skip, limit, request);
    }

    @Authenticate
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "/add-to-cart")
    @Operation(summary = "Add product to shopping cart. When no cart exists, this will create a new cart.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Add to cart successfully", content = @Content(schema = @Schema(implementation = CartGetDetailResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = UnauthorizedException.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ForbiddenException.class))),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = NotFoundException.class))),
        @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = BadRequestException.class)))})
    public CompletableFuture<CartGetDetailResponse> createCart(
        @Valid @RequestBody CartItemVm cartItemRequests, HttpServletRequest request) {
        return cartService.addToCart(cartItemRequests, request);
    }

    @Authenticate
    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{id}/quantity")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Update successfully", content = @Content(schema = @Schema())),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = UnauthorizedException.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ForbiddenException.class))),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = NotFoundException.class))),
        @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = BadRequestException.class)))})
    @Operation(summary = "Update quantity cart item")
    public CompletableFuture<Void> updateQuantity(@Valid @RequestBody UpdateQuantityRequest input,
        HttpServletRequest request) {
        return cartService.updateQuantity(input, request);
    }

    @Authenticate
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/multi-delete")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Delete successfully", content = @Content(schema = @Schema())),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = UnauthorizedException.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ForbiddenException.class))),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = NotFoundException.class))),
        @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = BadRequestException.class)))})
    @Operation(summary = "delete cart item")
    public CompletableFuture<Void> deleteCartItem(@Valid @RequestBody DeleteCartItemRequest input,
        HttpServletRequest request) {

        return cartService.deleteCartItem(input, request);
    }

    @Authenticate
    @GetMapping("/multi-cart")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "get cart successfully", content = @Content(schema = @Schema())),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = UnauthorizedException.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ForbiddenException.class))),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = NotFoundException.class))),
        @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = BadRequestException.class)))})
    @Operation(summary = "Get multi cart.")
    public CompletableFuture<List<CartGetDetailResponse>> getListCart(
        @RequestParam() List<String> cartIds
        , HttpServletRequest request) {

        return cartService.getListCart(cartIds, request);
    }
}
