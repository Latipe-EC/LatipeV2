package latipe.store.services.store;


import static latipe.store.constants.CONSTANTS.URL;
import static latipe.store.utils.GenTokenInternal.generateHash;
import static latipe.store.utils.GenTokenInternal.getPrivateKey;

import com.google.gson.Gson;
import feign.Feign;
import feign.Logger;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.okhttp.OkHttpClient;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import latipe.store.FeignClient.ProductClient;
import latipe.store.FeignClient.UserClient;
import latipe.store.configs.SecureInternalProperties;
import latipe.store.constants.Action;
import latipe.store.dtos.PagedResultDto;
import latipe.store.exceptions.BadRequestException;
import latipe.store.exceptions.NotFoundException;
import latipe.store.mapper.StoreMapper;
import latipe.store.producer.RabbitMQProducer;
import latipe.store.repositories.IStoreRepository;
import latipe.store.request.CreateStoreRequest;
import latipe.store.request.GetProvinceCodesRequest;
import latipe.store.request.MultipleStoreRequest;
import latipe.store.request.UpdateStoreRequest;
import latipe.store.response.ProvinceCodesResponse;
import latipe.store.response.StoreResponse;
import latipe.store.response.StoreSimplifyResponse;
import latipe.store.response.product.ProductStoreResponse;
import latipe.store.viewmodel.StoreMessage;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class StoreService implements IStoreService {

  private final IStoreRepository storeRepository;
  private final StoreMapper storeMapper;
  private final SecureInternalProperties secureInternalProperties;
  private final RabbitMQProducer rabbitMQProducer;
  private final Gson gson;

  @Override
  @Async
  public CompletableFuture<StoreResponse> create(String userId, CreateStoreRequest input,
      String token) {
    UserClient userClient = Feign.builder().client(new OkHttpClient()).encoder(new GsonEncoder())
        .decoder(new GsonDecoder()).logLevel(Logger.Level.FULL).target(UserClient.class, URL);

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
      String message = gson.toJson(
          StoreMessage.builder().id(store.getId()).op(Action.CREATE).build());
      rabbitMQProducer.sendMessage(message);
      return storeMapper.mapToStoreResponse(store);
    });
  }

  @Override
  @Async
  public CompletableFuture<StoreResponse> update(String userId, UpdateStoreRequest input) {
    return CompletableFuture.supplyAsync(() -> {

      var store = storeRepository.findByOwnerId(userId);
      if (store == null) {
        throw new NotFoundException("Store not found");
      }

      if (store.getAddress() == null) {
        throw new BadRequestException("Store address is not set");
      }

      if (store.getIsDeleted()) {
        throw new BadRequestException("Store is deleted");
      }
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
      var store = storeRepository.findByOwnerId(userId);

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
  public CompletableFuture<StoreResponse> getDetailStoreById(String storeId) {
    return CompletableFuture.supplyAsync(() -> {
      var store = storeRepository.findById(storeId)
          .orElseThrow(() -> new NotFoundException("Store not found"));
      return storeMapper.mapToStoreResponse(store);
    });
  }

  @Override
  @Async
  public CompletableFuture<ProvinceCodesResponse> getProvinceCodes(GetProvinceCodesRequest input) {
    return CompletableFuture.supplyAsync(() -> {
      Set<String> storeIds = Set.copyOf(input.ids());
      List<String> codes = storeRepository.findByIdIn(input.ids()).stream()
          .map(x -> x.getAddress().getCityOrProvinceId()).toList();
      if (codes.size() != storeIds.size()) {
        throw new BadRequestException("Invalid province id");
      }
      return ProvinceCodesResponse.builder().codes(codes).build();
    });
  }

  @Override
  @Async
  public CompletableFuture<PagedResultDto<ProductStoreResponse>> getMyProductStore(long skip,
      int limit, String name, String orderBy, String userId) {
    return CompletableFuture.supplyAsync(() -> {
      var store = storeRepository.findByOwnerId(userId);
      if (store.getIsBan()) {
        throw new BadRequestException("Store is banned");
      }
      if (store.getIsDeleted()) {
        throw new BadRequestException("Store is deleted");
      }
      String hash;
      try {
        hash = generateHash("product-service",
            getPrivateKey(secureInternalProperties.getPrivateKey()));
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

      var productClient = Feign.builder().client(new OkHttpClient()).encoder(new GsonEncoder())
          .decoder(new GsonDecoder()).logLevel(Logger.Level.FULL).target(ProductClient.class, URL);

      return productClient.getProductStore(hash, name, skip, limit, orderBy, store.getId());
    });
  }

  @Override
  @Async
  public CompletableFuture<PagedResultDto<ProductStoreResponse>> getBanProductStore(long skip,
      int limit, String name, String orderBy, String userId) {
    return CompletableFuture.supplyAsync(() -> {

      var store = storeRepository.findByOwnerId(userId);

      if (store.getIsBan()) {
        throw new BadRequestException("Store is banned");
      }
      if (store.getIsDeleted()) {
        throw new BadRequestException("Store is deleted");
      }

      String hash;
      try {
        hash = generateHash("product-service",
            getPrivateKey(secureInternalProperties.getPrivateKey()));
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

      var productClient = Feign.builder().client(new OkHttpClient()).encoder(new GsonEncoder())
          .decoder(new GsonDecoder()).logLevel(Logger.Level.FULL).target(ProductClient.class, URL);

      return productClient.getBanProductStore(hash, name, skip, limit, orderBy, store.getId());
    });

  }

  @Override
  @Async
  public CompletableFuture<List<StoreSimplifyResponse>> getMultipleStore(
      MultipleStoreRequest input) {
    return CompletableFuture.supplyAsync(() -> {
      var stores = storeRepository.findByIdIn(input.ids());
      if (stores.size() != input.ids().size()) {
        throw new BadRequestException("Invalid store id");
      }
      return stores.stream().map(storeMapper::mapToStoreSimplifyResponse).toList();
    });
  }
}
