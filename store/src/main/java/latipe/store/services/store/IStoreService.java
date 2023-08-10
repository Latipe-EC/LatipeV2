package latipe.store.services.store;


import latipe.store.services.IService;
import latipe.store.services.store.Dtos.StoreCreateDto;
import latipe.store.services.store.Dtos.StoreDto;
import latipe.store.services.store.Dtos.StoreUpdateDto;
import org.springframework.scheduling.annotation.Async;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CompletableFuture;

public interface IStoreService extends IService<StoreDto, StoreCreateDto, StoreUpdateDto> {
    @Async
    CompletableFuture<StoreDto> create(String userId, StoreCreateDto input);

    @Async
    CompletableFuture<StoreDto> update(String userId, String storeId, StoreUpdateDto input) throws InvocationTargetException, IllegalAccessException;

    @Async
    CompletableFuture<String> getStoreByUserId(String userId);
//    @Query("{'id' : ?0}")
//    Store findById(String id);
}

