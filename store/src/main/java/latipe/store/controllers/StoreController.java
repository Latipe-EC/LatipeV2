package latipe.store.controllers;

import latipe.store.annotations.ApiPrefixController;
import latipe.store.annotations.Authenticate;
import latipe.store.dtos.UserCredentialDto;
import latipe.store.services.store.Dtos.StoreCreateDto;
import latipe.store.services.store.Dtos.StoreDto;
import latipe.store.services.store.IStoreService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@ApiPrefixController("store")
public class StoreController {

    private final IStoreService storeService;

    public StoreController(IStoreService storeService) {
        this.storeService = storeService;
    }

    @Authenticate
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/validate-store/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<String> validateStore(@PathVariable String userId) {
        return storeService.getStoreByUserId(userId);
    }

    @Authenticate
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/create-store", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<StoreDto> createStore(
            @RequestAttribute(value = "user") UserCredentialDto userCredential,
            @RequestBody StoreCreateDto input) {
        return storeService.create(userCredential.getId(), input);
    }
}
