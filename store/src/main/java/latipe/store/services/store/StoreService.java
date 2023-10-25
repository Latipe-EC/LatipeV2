package latipe.store.services.store;


import feign.Feign;
import feign.Logger;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.okhttp.OkHttpClient;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import latipe.store.Entity.Store;
import latipe.store.FeignClient.UserClient;
import latipe.store.exceptions.BadRequestException;
import latipe.store.exceptions.NotFoundException;
import latipe.store.mapper.StoreMapper;
import latipe.store.repositories.IStoreRepository;
import latipe.store.request.CreateStoreRequest;
import latipe.store.request.GetProvinceCodesRequest;
import latipe.store.request.UpdateStoreRequest;
import latipe.store.response.ProvinceCodesResponse;
import latipe.store.response.StoreResponse;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class StoreService implements IStoreService {

  private final IStoreRepository storeRepository;
  private final StoreMapper storeMapper;


  @Override
  @Async
  @Transactional
  public CompletableFuture<StoreResponse> create(String userId, CreateStoreRequest input,
      String token) {
    UserClient userClient = Feign.builder().client(new OkHttpClient()).encoder(new GsonEncoder())
        .decoder(new GsonDecoder()).logLevel(Logger.Level.FULL)
        .target(UserClient.class, "http://localhost:8181/api/v1");

    return CompletableFuture.supplyAsync(() -> {

      var store = storeRepository.findByOwnerId(userId);
      if (store != null) {
        throw new BadRequestException("One User can only have one store");
      }

      var existingName = storeRepository.existsByName(input.name());
      if (existingName) {
        throw new BadRequestException("Store name already exists");
      }

      store = storeMapper.mapToStoreBeforeCreate(input, userId);
      store = storeRepository.save(store);

      // update role store
      userClient.upgradeVendor(token);
      return storeMapper.mapToStoreResponse(store);
    });
  }

  @Override
  @Async
  @Transactional
  public CompletableFuture<StoreResponse> update(String userId, String storeId,
      UpdateStoreRequest input) {
    return CompletableFuture.supplyAsync(() -> {

      Store store = storeRepository.findById(storeId)
          .orElseThrow(() -> new NotFoundException("Store not found"));
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

      if (store.getAddress() == null) {
        throw new BadRequestException("Store address is not set");
      }

      if (store.getIsDeleted()) {
        throw new BadRequestException("Store is deleted");
      }
      return store.getId();
    });
  }

  @Override
  @Async
  public CompletableFuture<ProvinceCodesResponse> getProvinceCodes(GetProvinceCodesRequest input) {
    return CompletableFuture.supplyAsync(() -> {
      Set<String> storeIds = Set.copyOf(input.ids());
      List<Long> codes = storeRepository.findByIdIn(input.ids()).stream()
          .map(x -> x.getAddress().getCityOrProvinceId()).toList();
      if (codes.size() != storeIds.size()) {
        throw new BadRequestException("Invalid province id");
      }
      return ProvinceCodesResponse.builder().codes(codes).build();
    });
  }

}
