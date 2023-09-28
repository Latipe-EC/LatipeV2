package latipe.store.services.store;


import latipe.store.request.CreateStoreRequest;
import latipe.store.response.StoreResponse;
import latipe.store.request.UpdateStoreRequest;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CompletableFuture;

public interface IStoreService  {
    CompletableFuture<StoreResponse> create(String userId, CreateStoreRequest input);

    CompletableFuture<StoreResponse> update(String userId, String storeId, UpdateStoreRequest input) throws InvocationTargetException, IllegalAccessException;

    CompletableFuture<String> getStoreByUserId(String userId);

}
