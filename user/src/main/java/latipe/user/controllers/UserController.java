package latipe.user.controllers;

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
import latipe.user.response.UserCredentialResponse;
import latipe.user.response.UserResponse;
import latipe.user.services.user.IUserService;
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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Validated
@RestController
@ApiPrefixController("/users")
public class UserController {

  private final IUserService userService;

  public UserController(IUserService userService) {
    this.userService = userService;
  }

  @ResponseStatus(HttpStatus.OK)
  @Authenticate
  @GetMapping(value = "/my-profile", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<UserResponse> getMyProfile() {
    UserCredentialResponse userCredential = ((UserCredentialResponse) (((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest()
        .getAttribute("user")));
    return userService.getProfile(userCredential.id());
  }

  @ResponseStatus(HttpStatus.OK)
  @Authenticate
  @PutMapping(value = "/my-profile", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<UserResponse> updateProfile(
      @Valid @RequestBody UpdateUserRequest input) {
    UserCredentialResponse userCredential = ((UserCredentialResponse) (((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest()
        .getAttribute("user")));
    return userService.updateProfile(userCredential.id(), input);
  }

  @ResponseStatus(HttpStatus.OK)
  @Authenticate
  @GetMapping(value = "/my-address", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<PagedResultDto<UserAddress>> getMyAddresses(
      @RequestParam(name = "page", defaultValue = "1") int page,
      @RequestParam(name = "size", defaultValue = "10") int size
  ) {
    UserCredentialResponse userCredential = ((UserCredentialResponse) (((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest()
        .getAttribute("user")));
    return userService.getMyAddresses(userCredential.id(), page, size);
  }

  @ResponseStatus(HttpStatus.OK)
  @Authenticate
  @PutMapping(value = "/my-address/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<UserAddress> updateMyAddress(
      @PathVariable String id,
      @Valid @RequestBody UpdateUserAddressRequest input) {
    UserCredentialResponse userCredential = ((UserCredentialResponse) (((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest()
        .getAttribute("user")));
    return userService.updateMyAddresses(input, userCredential.id(), id);
  }

  @Authenticate
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/my-address", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<UserAddress> addMyAddress(
      @Valid @RequestBody CreateUserAddressRequest input) {

    UserCredentialResponse userCredential = ((UserCredentialResponse) (((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest()
        .getAttribute("user")));
    return userService.addMyAddresses(userCredential.id(), input);

  }

  @ResponseStatus(HttpStatus.OK)
  @Authenticate
  @DeleteMapping(value = "/my-address/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<Void> deleteMyAddress(@PathVariable String id) {

    UserCredentialResponse userCredential = ((UserCredentialResponse) (((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest()
        .getAttribute("user")));
    return userService.deleteMyAddresses(id, userCredential.id());

  }

  @ResponseStatus(HttpStatus.OK)
  @Authenticate
  @GetMapping(value = "/my-address/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<UserAddress> getMyAddress(@PathVariable String id) {

    UserCredentialResponse userCredential = ((UserCredentialResponse) (((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest()
        .getAttribute("user")));
    return userService.getMyAddresses(id, userCredential.id());

  }

  @Authenticate
  @ResponseStatus(HttpStatus.CREATED)
  @GetMapping(value = "/count-my-address", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<Integer> countMyUserAddress() {
    UserCredentialResponse userCredential = ((UserCredentialResponse) (((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest()
        .getAttribute("user")));
    return userService.countMyAddress(userCredential.id());
  }

  @ResponseStatus(HttpStatus.CREATED)
  @RequiresAuthorization("ADMIN")
  @PostMapping(value = "/create-user", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<UserResponse> createUser(@Valid @RequestBody CreateUserRequest input) {
    return userService.create(input);
  }

  @RequiresAuthorization("ADMIN")
  @GetMapping(value = "/admin", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<PagedResultDto<UserAdminResponse>> getUserAdmin(
      @RequestParam(defaultValue = "") String keyword,
      @RequestParam(defaultValue = "0") Long skip,
      @RequestParam(defaultValue = "12") Integer size,
      @RequestParam(defaultValue = "createdDate") String orderBy,
      @RequestParam(defaultValue = "ALL") EStatusBan isBan) {
    return userService.getUserAdmin(keyword, skip, size, orderBy, isBan);
  }

  @RequiresAuthorization("ADMIN")
  @PatchMapping(value = "/{userId}/ban", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<Void> banUser(
      @PathVariable String userId,
      @Valid @RequestBody BanUserRequest request) {
    return userService.banUser(userId, request);
  }

  @RequiresAuthorization("ADMIN")
  @GetMapping(value = "/{userId}/admin", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<UserResponse> getDetailUserByAdmin(
      @PathVariable String userId) {
    return userService.getProfile(userId);
  }

  @RequiresAuthorization("ADMIN")
  @GetMapping(value = "/count", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<Long> countAllUser() {
    return userService.countAllUser();
  }

  @SecureInternalPhase
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<UserResponse> register(@RequestBody RegisterRequest input) {
    return userService.register(input);
  }

  @Authenticate
  @ResponseStatus(HttpStatus.CREATED)
  @PutMapping(value = "/upgrade-to-vendor", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<Void> upgradeVendor() {
    var userCredential = ((UserCredentialResponse) (((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest()
        .getAttribute("user")));
    return userService.upgradeVendor(userCredential.id());
  }


  @SecureInternalPhase
  @PostMapping(value = "/check-balance", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<Void> checkBalance(
      @RequestBody CheckBalanceRequest request
  ) {
    return userService.checkBalance(request);
  }

  @SecureInternalPhase
  @PostMapping(value = "/cancel-order", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<Void> cancelOrder(
      @RequestBody CancelOrderRequest request
  ) {
    return userService.cancelOrder(request);
  }

  @Authenticate
  @ResponseStatus(HttpStatus.CREATED)
  @PutMapping(value = "/profile/username", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<Void> updateUserName(
      @Valid @RequestBody UpdateUserNameRequest request
  ) {
    UserCredentialResponse userCredential = ((UserCredentialResponse) (((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest()
        .getAttribute("user")));
    return userService.updateUserName(request, userCredential.id());
  }

  @SecureInternalPhase
  @GetMapping(value = "/{userId}/internal/info-rating", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<InfoRatingResponse> getInfoForRating(
      @PathVariable String userId
  ) {
    return userService.getInfoForRating(userId);
  }

}
