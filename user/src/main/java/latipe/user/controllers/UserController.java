package latipe.user.controllers;

import jakarta.validation.Valid;
import latipe.user.Entity.UserAddress;
import latipe.user.annotations.ApiPrefixController;
import latipe.user.annotations.Authenticate;
import latipe.user.dtos.UserCredentialDto;
import latipe.user.services.user.Dtos.CreateUserAddressDto;
import latipe.user.services.user.Dtos.UserCreateDto;
import latipe.user.services.user.Dtos.UserDto;
import latipe.user.services.user.IUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@ApiPrefixController("/user")
@Validated
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
    public CompletableFuture<List<UserAddress>> getMyAddresses(@RequestAttribute(value = "user") UserCredentialDto userCredential,
                                                               @RequestParam(name = "page", defaultValue = "0") int page,
                                                               @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return userService.getMyAddresses(userCredential.getId(), page, size);
    }

    @ResponseStatus(HttpStatus.OK)
    @Authenticate
    @PatchMapping(value = "/my-address/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<UserAddress> updateMyAddress(@RequestAttribute(value = "user") UserCredentialDto userCredential,
                                                          @PathVariable String id, UserAddress input) {
        return userService.updateMyAddresses(input, userCredential.getId(), id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @Authenticate
    @PostMapping(value = "/my-address", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<UserAddress> addMyAddress(@RequestAttribute(value = "user") UserCredentialDto userCredential, CreateUserAddressDto input) {
        return userService.addMyAddresses(userCredential.getId(), input);
    }

    @ResponseStatus(HttpStatus.OK)
    @Authenticate
    @DeleteMapping(value = "/my-address/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<Void> deleteMyAddress(@PathVariable String id) {
        return userService.deleteMyAddresses(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/create-user", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<UserDto> createUser(@Valid @RequestBody UserCreateDto input) {
        return userService.create(input);
    }
}
