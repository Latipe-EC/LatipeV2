package latipe.user.controllers;

import static latipe.user.utils.Constants.ADMIN;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.concurrent.CompletableFuture;
import latipe.user.annotations.ApiPrefixController;
import latipe.user.annotations.Authenticate;
import latipe.user.annotations.RequiresAuthorization;
import latipe.user.annotations.SecureInternalPhase;
import latipe.user.constants.EStatusBan;
import latipe.user.dtos.PagedResultDto;
import latipe.user.entity.UserAddress;
import latipe.user.request.BanUserRequest;
import latipe.user.request.CancelOrderRequest;
import latipe.user.request.CheckBalanceRequest;
import latipe.user.request.CreateUserAddressRequest;
import latipe.user.request.CreateUserRequest;
import latipe.user.request.RegisterRequest;
import latipe.user.request.UpdateUserAddressRequest;
import latipe.user.request.UpdateUserNameRequest;
import latipe.user.request.UpdateUserRequest;
import latipe.user.response.InfoRatingResponse;
import latipe.user.response.UserAdminResponse;
import latipe.user.response.UserResponse;
import latipe.user.services.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller that manages user-related operations.
 * This includes user profile management, address management, administration functions,
 * and other user-centric operations.
 */
@Validated
@RestController
@ApiPrefixController("/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    /**
     * Retrieves the profile of the authenticated user.
     *
     * @param request The HTTP request containing authentication details
     * @return CompletableFuture containing the user profile information
     */
    @ResponseStatus(HttpStatus.OK)
    @Authenticate
    @GetMapping(value = "/my-profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<UserResponse> getMyProfile(HttpServletRequest request) {

        return userService.getProfile(request);
    }

    /**
     * Updates the profile information of the authenticated user.
     *
     * @param input The user information to update
     * @param request The HTTP request containing authentication details
     * @return CompletableFuture containing the updated user profile
     */
    @ResponseStatus(HttpStatus.OK)
    @Authenticate
    @PutMapping(value = "/my-profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<UserResponse> updateProfile(
        @Valid @RequestBody UpdateUserRequest input, HttpServletRequest request) {

        return userService.updateProfile(input, request);
    }

    /**
     * Retrieves a paginated list of addresses for the authenticated user.
     *
     * @param page The page number (1-based indexing)
     * @param size The number of items per page
     * @param request The HTTP request containing authentication details
     * @return CompletableFuture containing a paginated list of user addresses
     */
    @ResponseStatus(HttpStatus.OK)
    @Authenticate
    @GetMapping(value = "/my-address", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<PagedResultDto<UserAddress>> getMyAddresses(
        @RequestParam(name = "page", defaultValue = "1") int page,
        @RequestParam(name = "size", defaultValue = "10") int size, HttpServletRequest request
    ) {
        return userService.getMyAddresses(page, size, request);
    }

    /**
     * Updates an existing address for the authenticated user.
     *
     * @param id The ID of the address to update
     * @param input The updated address information
     * @param request The HTTP request containing authentication details
     * @return CompletableFuture containing the updated address
     */
    @ResponseStatus(HttpStatus.OK)
    @Authenticate
    @PutMapping(value = "/my-address/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<UserAddress> updateMyAddress(
        @PathVariable String id,
        @Valid @RequestBody UpdateUserAddressRequest input, HttpServletRequest request) {
        return userService.updateMyAddresses(input, id, request);
    }

    /**
     * Adds a new address for the authenticated user.
     *
     * @param input The address information to add
     * @param request The HTTP request containing authentication details
     * @return CompletableFuture containing the newly created address
     */
    @Authenticate
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/my-address", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<UserAddress> addMyAddress(
        @Valid @RequestBody CreateUserAddressRequest input, HttpServletRequest request) {

        return userService.addMyAddresses(input, request);

    }

    /**
     * Deletes an address for the authenticated user.
     *
     * @param id The ID of the address to delete
     * @param request The HTTP request containing authentication details
     * @return CompletableFuture indicating completion of the operation
     */
    @ResponseStatus(HttpStatus.OK)
    @Authenticate
    @DeleteMapping(value = "/my-address/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<Void> deleteMyAddress(@PathVariable String id,
        HttpServletRequest request) {

        return userService.deleteMyAddresses(id, request);

    }

    /**
     * Retrieves a specific address for the authenticated user.
     *
     * @param id The ID of the address to retrieve
     * @param request The HTTP request containing authentication details
     * @return CompletableFuture containing the requested address
     */
    @ResponseStatus(HttpStatus.OK)
    @Authenticate
    @GetMapping(value = "/my-address/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<UserAddress> getMyAddress(@PathVariable String id,
        HttpServletRequest request) {

        return userService.getMyAddresses(id, request);

    }

    /**
     * Counts the number of addresses for the authenticated user.
     *
     * @param request The HTTP request containing authentication details
     * @return CompletableFuture containing the count of user addresses
     */
    @Authenticate
    @GetMapping(value = "/count-my-address", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<Integer> countMyUserAddress(HttpServletRequest request) {
        return userService.countMyAddress(request);
    }

    /**
     * Creates a new user account (admin only).
     *
     * @param input The user information for the new account
     * @param request The HTTP request containing authentication details
     * @return CompletableFuture containing the newly created user profile
     */
    @ResponseStatus(HttpStatus.CREATED)
    @RequiresAuthorization(ADMIN)
    @PostMapping(value = "/create-user", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<UserResponse> createUser(@Valid @RequestBody CreateUserRequest input,
        HttpServletRequest request) {
        return userService.create(input, request);
    }

    /**
     * Retrieves a paginated list of users for administrative purposes.
     *
     * @param keyword Search keyword to filter users
     * @param skip Number of records to skip
     * @param size Number of records to return
     * @param orderBy Field to order results by
     * @param isBan Filter by ban status
     * @param request The HTTP request containing authentication details
     * @return CompletableFuture containing a paginated list of user information
     */
    @RequiresAuthorization(ADMIN)
    @GetMapping(value = "/admin", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<PagedResultDto<UserAdminResponse>> getUserAdmin(
        @RequestParam(defaultValue = "") String keyword,
        @RequestParam(defaultValue = "0") Long skip,
        @RequestParam(defaultValue = "12") Integer size,
        @RequestParam(defaultValue = "createdDate") String orderBy,
        @RequestParam(defaultValue = "ALL") EStatusBan isBan, HttpServletRequest request) {
        return userService.getUserAdmin(keyword, skip, size, orderBy, isBan, request);
    }

    /**
     * Bans or unbans a user (admin only).
     *
     * @param userId The ID of the user to ban/unban
     * @param input The ban information including reason and duration
     * @param request The HTTP request containing authentication details
     * @return CompletableFuture indicating completion of the operation
     */
    @RequiresAuthorization(ADMIN)
    @PatchMapping(value = "/{userId}/ban", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<Void> banUser(
        @PathVariable String userId,
        @Valid @RequestBody BanUserRequest input, HttpServletRequest request) {
        return userService.banUser(userId, input, request);
    }

    /**
     * Retrieves detailed user information for administrative purposes.
     *
     * @param userId The ID of the user to retrieve
     * @param request The HTTP request containing authentication details
     * @return CompletableFuture containing the detailed user profile
     */
    @RequiresAuthorization(ADMIN)
    @GetMapping(value = "/{userId}/admin", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<UserResponse> getDetailUserByAdmin(
        @PathVariable String userId, HttpServletRequest request) {
        return userService.getUserByAdmin(userId, request);
    }

    /**
     * Counts the total number of users in the system (admin only).
     *
     * @param request The HTTP request containing authentication details
     * @return CompletableFuture containing the total user count
     */
    @RequiresAuthorization(ADMIN)
    @GetMapping(value = "/count", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<Long> countAllUser(HttpServletRequest request) {
        return userService.countAllUser(request);
    }

    /**
     * Registers a new user (internal service use only).
     *
     * @param input The registration information
     * @param request The HTTP request containing service authentication details
     * @return CompletableFuture containing the newly registered user profile
     */
    @SecureInternalPhase
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<UserResponse> register(@RequestBody RegisterRequest input,
        HttpServletRequest request) {
        return userService.register(input, request);
    }

    /**
     * Upgrades a regular user to vendor status.
     *
     * @param request The HTTP request containing authentication details
     * @return CompletableFuture indicating completion of the operation
     */
    @Authenticate
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping(value = "/upgrade-to-vendor", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<Void> upgradeVendor(HttpServletRequest request) {
        return userService.upgradeVendor(request);
    }

    /**
     * Checks if a user has sufficient balance for an operation (internal service use only).
     *
     * @param input The balance check request details
     * @param request The HTTP request containing service authentication details
     * @return CompletableFuture indicating completion of the operation
     */
    @SecureInternalPhase
    @PostMapping(value = "/check-balance", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<Void> checkBalance(
        @RequestBody CheckBalanceRequest input, HttpServletRequest request
    ) {
        return userService.checkBalance(input, request);
    }

    /**
     * Processes a canceled order, potentially refunding a user (internal service use only).
     *
     * @param input The order cancellation details
     * @param request The HTTP request containing service authentication details
     * @return CompletableFuture indicating completion of the operation
     */
    @SecureInternalPhase
    @PostMapping(value = "/cancel-order", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<Void> cancelOrder(
        @RequestBody CancelOrderRequest input, HttpServletRequest request
    ) {
        return userService.cancelOrder(input, request);
    }

    /**
     * Updates the username of the authenticated user.
     *
     * @param input The new username information
     * @param request The HTTP request containing authentication details
     * @return CompletableFuture indicating completion of the operation
     */
    @Authenticate
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping(value = "/profile/username", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<Void> updateUserName(
        @Valid @RequestBody UpdateUserNameRequest input, HttpServletRequest request
    ) {

        return userService.updateUserName(input, request);
    }

    /**
     * Retrieves user information for rating purposes (internal service use only).
     *
     * @param userId The ID of the user to retrieve information for
     * @param request The HTTP request containing service authentication details
     * @return CompletableFuture containing the user information needed for ratings
     */
    @SecureInternalPhase
    @GetMapping(value = "/{userId}/internal/info-rating", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<InfoRatingResponse> getInfoForRating(
        @PathVariable String userId, HttpServletRequest request
    ) {
        return userService.getInfoForRating(userId, request);
    }

}
