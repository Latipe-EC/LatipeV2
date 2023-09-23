package latipe.user.controllers;

import jakarta.validation.Valid;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import latipe.user.Entity.UserAddress;
import latipe.user.annotations.ApiPrefixController;
import latipe.user.annotations.Authenticate;
import latipe.user.dtos.UserCredentialDto;
import latipe.user.services.user.Dtos.CreateUserAddressDto;
import latipe.user.services.user.Dtos.UserCreateDto;
import latipe.user.services.user.Dtos.UserDto;
import latipe.user.services.user.IUserService;
import latipe.user.viewmodel.RegisterRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

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
  public CompletableFuture<String> getMyProfile() {
    return CompletableFuture.completedFuture("Hello");
  }


  @ResponseStatus(HttpStatus.OK)
  @Authenticate
  @PatchMapping(value = "/my-profile", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<String> updateProfile() {
    return CompletableFuture.completedFuture("Hello");
  }

  @ResponseStatus(HttpStatus.OK)
  @Authenticate
  @GetMapping(value = "/my-address", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<List<UserAddress>> getMyAddresses(
      @RequestAttribute(value = "user") UserCredentialDto userCredential,
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "10") int size
  ) {
    return userService.getMyAddresses(userCredential.getId(), page, size);
  }

  @ResponseStatus(HttpStatus.OK)
  @Authenticate
  @PatchMapping(value = "/my-address/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<UserAddress> updateMyAddress(
      @RequestAttribute(value = "user") UserCredentialDto userCredential,
      @PathVariable String id, UserAddress input) {
    return userService.updateMyAddresses(input, userCredential.getId(), id);
  }

  @ResponseStatus(HttpStatus.CREATED)
  @Authenticate
  @PostMapping(value = "/my-address", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<UserAddress> addMyAddress(
      @RequestAttribute(value = "user") UserCredentialDto userCredential,
      CreateUserAddressDto input) {
    return userService.addMyAddresses(userCredential.getId(), input);
  }

  @ResponseStatus(HttpStatus.OK)
  @Authenticate
  @DeleteMapping(value = "/my-address/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<Void> deleteMyAddress(@PathVariable String id) {
    return userService.deleteMyAddresses(id);
  }

  @ResponseStatus(HttpStatus.CREATED)
  @Authenticate
  @PostMapping(value = "/create-user", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<UserDto> createUser(@Valid @RequestBody UserCreateDto input) {
    return userService.create(input);
  }

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<UserDto> register(@Valid @RequestBody RegisterRequest input) {
    return userService.register(input);
  }
}
