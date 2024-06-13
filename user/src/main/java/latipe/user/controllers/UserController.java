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

@Validated
@RestController
@ApiPrefixController("/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    @ResponseStatus(HttpStatus.OK)
    @Authenticate
    @GetMapping(value = "/my-profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<UserResponse> getMyProfile(HttpServletRequest request) {

        return userService.getProfile(request);
    }

    @ResponseStatus(HttpStatus.OK)
    @Authenticate
    @PutMapping(value = "/my-profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<UserResponse> updateProfile(
        @Valid @RequestBody UpdateUserRequest input, HttpServletRequest request) {

        return userService.updateProfile(input, request);
    }

    @ResponseStatus(HttpStatus.OK)
    @Authenticate
    @GetMapping(value = "/my-address", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<PagedResultDto<UserAddress>> getMyAddresses(
        @RequestParam(name = "page", defaultValue = "1") int page,
        @RequestParam(name = "size", defaultValue = "10") int size, HttpServletRequest request
    ) {
        return userService.getMyAddresses(page, size, request);
    }

    @ResponseStatus(HttpStatus.OK)
    @Authenticate
    @PutMapping(value = "/my-address/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<UserAddress> updateMyAddress(
        @PathVariable String id,
        @Valid @RequestBody UpdateUserAddressRequest input, HttpServletRequest request) {
        return userService.updateMyAddresses(input, id, request);
    }

    @Authenticate
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/my-address", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<UserAddress> addMyAddress(
        @Valid @RequestBody CreateUserAddressRequest input, HttpServletRequest request) {

        return userService.addMyAddresses(input, request);

    }

    @ResponseStatus(HttpStatus.OK)
    @Authenticate
    @DeleteMapping(value = "/my-address/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<Void> deleteMyAddress(@PathVariable String id,
        HttpServletRequest request) {

        return userService.deleteMyAddresses(id, request);

    }

    @ResponseStatus(HttpStatus.OK)
    @Authenticate
    @GetMapping(value = "/my-address/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<UserAddress> getMyAddress(@PathVariable String id,
        HttpServletRequest request) {

        return userService.getMyAddresses(id, request);

    }

    @Authenticate
    @ResponseStatus(HttpStatus.CREATED)
    @GetMapping(value = "/count-my-address", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<Integer> countMyUserAddress(HttpServletRequest request) {
        return userService.countMyAddress(request);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequiresAuthorization(ADMIN)
    @PostMapping(value = "/create-user", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<UserResponse> createUser(@Valid @RequestBody CreateUserRequest input,
        HttpServletRequest request) {
        return userService.create(input, request);
    }

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

    @RequiresAuthorization(ADMIN)
    @PatchMapping(value = "/{userId}/ban", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<Void> banUser(
        @PathVariable String userId,
        @Valid @RequestBody BanUserRequest input, HttpServletRequest request) {
        return userService.banUser(userId, input, request);
    }

    @RequiresAuthorization(ADMIN)
    @GetMapping(value = "/{userId}/admin", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<UserResponse> getDetailUserByAdmin(
        @PathVariable String userId, HttpServletRequest request) {
        return userService.getUserByAdmin(userId, request);
    }

    @RequiresAuthorization(ADMIN)
    @GetMapping(value = "/count", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<Long> countAllUser(HttpServletRequest request) {
        return userService.countAllUser(request);
    }

    @SecureInternalPhase
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<UserResponse> register(@RequestBody RegisterRequest input,
        HttpServletRequest request) {
        return userService.register(input, request);
    }

    @Authenticate
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping(value = "/upgrade-to-vendor", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<Void> upgradeVendor(HttpServletRequest request) {
        return userService.upgradeVendor(request);
    }


    @SecureInternalPhase
    @PostMapping(value = "/check-balance", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<Void> checkBalance(
        @RequestBody CheckBalanceRequest input, HttpServletRequest request
    ) {
        return userService.checkBalance(input, request);
    }

    @SecureInternalPhase
    @PostMapping(value = "/cancel-order", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<Void> cancelOrder(
        @RequestBody CancelOrderRequest input, HttpServletRequest request
    ) {
        return userService.cancelOrder(input, request);
    }

    @Authenticate
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping(value = "/profile/username", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<Void> updateUserName(
        @Valid @RequestBody UpdateUserNameRequest input, HttpServletRequest request
    ) {

        return userService.updateUserName(input, request);
    }

    @SecureInternalPhase
    @GetMapping(value = "/{userId}/internal/info-rating", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<InfoRatingResponse> getInfoForRating(
        @PathVariable String userId, HttpServletRequest request
    ) {
        return userService.getInfoForRating(userId, request);
    }

}
