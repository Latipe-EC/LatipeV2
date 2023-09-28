package latipe.store.services.store;


import java.util.concurrent.CompletableFuture;
import latipe.store.Entity.Store;
import latipe.store.exceptions.BadRequestException;
import latipe.store.exceptions.NotFoundException;
import latipe.store.mapper.StoreMapper;
import latipe.store.repositories.IStoreRepository;
import latipe.store.request.CreateStoreRequest;
import latipe.store.request.UpdateStoreRequest;
import latipe.store.response.StoreResponse;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class StoreService implements IStoreService {

  private final IStoreRepository storeRepository;
  private final StoreMapper storeMapper;


  @Override
  @Async
  public CompletableFuture<StoreResponse> create(String userId, CreateStoreRequest input) {
    return CompletableFuture.supplyAsync(() -> {
      Store store = storeRepository.findByOwnerId(userId);
      if (store != null) {
        throw new BadRequestException("One User can only have one store");
      }
      store = storeMapper.mapToStoreBeforeCreate(input, userId);
      store = storeRepository.save(store);
      return storeMapper.mapToStoreResponse(store);
    });
  }

  @Override
  @Async
  public CompletableFuture<StoreResponse> update(String userId, String storeId,
      UpdateStoreRequest input) {
    return CompletableFuture.supplyAsync(() -> {

      Store store = storeRepository.findById(storeId).orElseThrow(
          () -> new NotFoundException("Store not found")
      );
      if (!store.getOwnerId().equals(userId)) {
        throw new BadRequestException("You are not the owner of this store");
      }
      storeMapper.mapToStoreBeforeUpdate(store, input);
      store = storeRepository.save(store);

      return storeMapper.mapToStoreResponse(store);
    });
  }

  @Override
  @Async
  public CompletableFuture<String> getStoreByUserId(String userId) {
    return CompletableFuture.supplyAsync(() -> {
      Store store = storeRepository.findByOwnerId(userId);
      if (store == null) {
        throw new BadRequestException("One User can only have one store");
      }
      if (store.getIsDeleted()) {
        throw new BadRequestException("Store is deleted");
      }
      return store.getId();
    });
  }

}
