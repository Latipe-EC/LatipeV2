package latipe.store.controllers;

import java.util.concurrent.CompletableFuture;
import latipe.store.annotations.ApiPrefixController;
import latipe.store.annotations.Authenticate;
import latipe.store.request.CreateStoreRequest;
import latipe.store.response.StoreResponse;
import latipe.store.response.UserCredentialResponse;
import latipe.store.services.store.IStoreService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestController
@ApiPrefixController("stores")
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
    public CompletableFuture<StoreResponse> createStore(

        @RequestBody CreateStoreRequest input) {
        UserCredentialResponse userCredential = ((UserCredentialResponse) (((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest()
            .getAttribute("user")));
        return storeService.create(userCredential.id(), input);
    }
}
