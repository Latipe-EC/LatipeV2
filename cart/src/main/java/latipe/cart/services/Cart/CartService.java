package latipe.cart.services.Cart;

import static latipe.cart.utils.AuthenticationUtils.getMethodName;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
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
import latipe.cart.request.ProductFeatureRequest;
import latipe.cart.request.UpdateQuantityRequest;
import latipe.cart.response.CartGetDetailResponse;
import latipe.cart.response.DeleteCartItemRequest;
import latipe.cart.response.UserCredentialResponse;
import latipe.cart.services.Product.ProductService;
import latipe.cart.viewmodel.CartItemVm;
import latipe.cart.viewmodel.LogMessage;
import latipe.cart.viewmodel.UpdateCartAfterOrderVm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service implementation for cart operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CartService implements ICartService {

    private final ICartRepository cartRepository;
    private final ProductService productService;
    private final Gson gson;

    /**
     * Adds an item to the user's cart asynchronously.
     * Extracts user information from the request.
     *
     * @param cartItemRequest The item details to add.
     * @param request         The HTTP servlet request to extract user context.
     * @return A CompletableFuture containing the detailed cart response.
     */
    @Async
    @Override
    public CompletableFuture<CartGetDetailResponse> addToCart(CartItemVm cartItemRequest,
        HttpServletRequest request) {
        // Potential Improvement: Extract necessary info (e.g., userId) from request
        // before this async method to avoid potential request scope issues.

        log.info(gson.toJson(
            LogMessage.create("Add item to cart: [productId: %s], [OptionId: %s]".formatted(
                    cartItemRequest.productId(), cartItemRequest.productOptionId()),
                request, getMethodName())));

        return CompletableFuture.supplyAsync(() -> {
            List<ProductFeatureRequest> productIds =
                List.of(new ProductFeatureRequest(cartItemRequest.productId(),
                    cartItemRequest.productOptionId()));

            // Call API to check all products will be added to cart are existed
            var productThumbnailResponseList = productService.getProducts(productIds);
            if (productThumbnailResponseList.isEmpty()) {
                throw new NotFoundException("Not found product");
            }

            var cart = cartRepository.findByUserIdAndProductOptionIdAndProductId(getUserId(request),
                cartItemRequest.productOptionId(), cartItemRequest.productId()).orElse(null);
            if (cart != null) {
                cart.setQuantity(cart.getQuantity() + cartItemRequest.quantity());
            } else {
                cart = new Cart();
                cart.setUserId(getUserId(request));
                cart.setProductId(cartItemRequest.productId());
                cart.setProductOptionId(cartItemRequest.productOptionId());
                cart.setQuantity(cartItemRequest.quantity());
            }
            cart = cartRepository.save(cart);

            log.info("Add item to cart successfully");
            return CartGetDetailResponse.fromModel(cart, productThumbnailResponseList.get(0));
        });

    }

    /**
     * Retrieves the current user's cart with pagination asynchronously.
     * Extracts user information from the request.
     *
     * @param skip    The number of items to skip.
     * @param size    The maximum number of items to return.
     * @param request The HTTP servlet request to extract user context.
     * @return A CompletableFuture containing a paged result of the cart details.
     */
    @Async
    @Override
    public CompletableFuture<PagedResultDto<CartGetDetailResponse>> getMyCart(
        long skip, int size, HttpServletRequest request) {
        // Potential Improvement: Extract necessary info (e.g., userId) from request
        // before this async method to avoid potential request scope issues.

        log.info(gson.toJson(
            LogMessage.create("Get my cart",
                request, getMethodName())));

        return CompletableFuture.supplyAsync(() -> {
            var carts = cartRepository.findMyCart(getUserId(request), skip, size);
            var count = cartRepository.countByUserId(getUserId(request));

            var productIds = carts.stream()
                .map(x -> new ProductFeatureRequest(x.getProductId(), x.getProductOptionId()))
                .toList();

            // Call API to check all products will be added to cart are existed
            if (productIds.isEmpty()) {
                return PagedResultDto.create(Pagination.create(count, skip, size),
                    new ArrayList<>());
            }

            var productThumbnailResponseList = productService.getProducts(productIds);

            if (productThumbnailResponseList.size() != productIds.size()) {
                throw new NotFoundException("Not found product");
            }

            var data = carts.stream().map(x -> {
                var productDetail = productThumbnailResponseList.stream()
                    .filter(y -> y.id().equals(x.getProductId())).findFirst().orElseThrow();
                return CartGetDetailResponse.fromModel(x, productDetail);
            }).toList();

            log.info("Get my cart successfully");
            return PagedResultDto.create(Pagination.create(count, skip, size), data);
        });
    }

    /**
     * Updates the quantity of an item in the user's cart asynchronously.
     * Extracts user information from the request.
     *
     * @param input   The request containing the item ID and new quantity.
     * @param request The HTTP servlet request to extract user context.
     * @return A CompletableFuture indicating completion.
     */
    @Async
    @Override
    public CompletableFuture<Void> updateQuantity(UpdateQuantityRequest input,
        HttpServletRequest request) {
        // Potential Improvement: Extract necessary info (e.g., userId) from request
        // before this async method to avoid potential request scope issues.

        log.info(gson.toJson(
            LogMessage.create(
                "Update quantity cart item: [cartId: %s], [quantity: %s]".formatted(input.id(),
                    input.quantity()),
                request, getMethodName())));

        return CompletableFuture.supplyAsync(() -> {
            var cart = cartRepository.findById(input.id()).orElseThrow(
                () -> new NotFoundException("Not found cart with %s cartId".formatted(input.id())));
            cart.setQuantity(input.quantity());

            cartRepository.save(cart);
            log.info("Update quantity cart item successfully");
            return null;
        });
    }

    /**
     * Deletes an item from the user's cart asynchronously.
     * Extracts user information from the request.
     *
     * @param input   The request containing the item ID to delete.
     * @param request The HTTP servlet request to extract user context.
     * @return A CompletableFuture indicating completion.
     */
    @Async
    @Override
    public CompletableFuture<Void> deleteCartItem(DeleteCartItemRequest input,
        HttpServletRequest request) {
        // Potential Improvement: Extract necessary info (e.g., userId) from request
        // before this async method to avoid potential request scope issues.

        log.info(gson.toJson(
            LogMessage.create(
                "Delete cart item: [cartIds: %s]".formatted(input.ids()),
                request, getMethodName())));
        return CompletableFuture.supplyAsync(() -> {
            Set<String> cartIds = new HashSet<>(input.ids());
            var carts = cartRepository.findAllByIdAndUserId(cartIds, getUserId(request));

            if (carts.size() != cartIds.size()) {
                throw new NotFoundException("Not found cart");
            }

            cartRepository.deleteAll(carts);
            log.info("Delete cart item successfully");
            return null;
        });
    }

    /**
     * Removes cart items after an order has been placed asynchronously.
     * This is typically called internally, e.g., by a message consumer.
     *
     * @param updateCartAfterOrderVm Details of the items to remove based on the order.
     * @return A CompletableFuture indicating completion.
     */
    @Async
    @Override
    public CompletableFuture<Void> removeCartItemAfterOrder(
        UpdateCartAfterOrderVm updateCartAfterOrderVm) {
        log.info(gson.toJson(
            LogMessage.create(
                "Remove cart item after order: [cartIds: %s]".formatted(
                    updateCartAfterOrderVm.cartIdVmList()),
                null, getMethodName())));
        return CompletableFuture.supplyAsync(() -> {
            var carts = cartRepository.findAllById(updateCartAfterOrderVm.cartIdVmList());
            cartRepository.deleteAll(carts);

            log.info("Remove cart item after order successfully");
            return null;
        });
    }

    /**
     * Retrieves a list of cart items by their IDs asynchronously.
     * Extracts user information from the request.
     *
     * @param cartIds The list of cart IDs to retrieve.
     * @param request The HTTP servlet request to extract user context.
     * @return A CompletableFuture containing the list of cart details.
     */
    @Async
    @Override
    public CompletableFuture<List<CartGetDetailResponse>> getListCart(List<String> cartIds,
        HttpServletRequest request) {
        log.info(gson.toJson(
            LogMessage.create(
                "Get list cart: [cartIds: %s]".formatted(cartIds),
                request, getMethodName())));

        return CompletableFuture.supplyAsync(() -> {
            Set<String> uniqueIds = new HashSet<>(cartIds);

            var carts = cartRepository.findAllByIdAndUserId(uniqueIds,
                getUserId(request));

            if (carts.size() != uniqueIds.size()) {
                throw new NotFoundException("Not found cart");
            }

            var productIds = carts.stream()
                .map(x -> new ProductFeatureRequest(x.getProductId(), x.getProductOptionId()))
                .toList();

            var productThumbnailResponseList = productService.getProducts(productIds);

            if (productThumbnailResponseList.size() != productIds.size()) {
                throw new NotFoundException("Not found product");
            }

            log.info("Get list cart successfully");

            return carts.stream().map(x -> {
                var productDetail = productThumbnailResponseList.stream()
                    .filter(y -> y.id().equals(x.getProductId()) && y.optionId().equals(x.getProductOptionId())).findFirst().orElseThrow();
                return CartGetDetailResponse.fromModel(x, productDetail);
            }).toList();

        });
    }

    private String getUserId(HttpServletRequest request) {
        return ((UserCredentialResponse) request.getAttribute("user")).id();
    }

}
