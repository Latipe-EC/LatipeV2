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
import latipe.store.configs.SecureInternalProperties;
import latipe.store.constants.Action;
import latipe.store.constants.EStatusBan;
import latipe.store.dtos.PagedResultDto;
import latipe.store.dtos.Pagination;
import latipe.store.entity.Store;
import latipe.store.exceptions.BadRequestException;
import latipe.store.exceptions.NotFoundException;
import latipe.store.feign.ProductClient;
import latipe.store.feign.UserClient;
import latipe.store.mapper.StoreMapper;
import latipe.store.producer.RabbitMQProducer;
import latipe.store.repositories.IStoreRepository;
import latipe.store.request.BanStoreRequest;
import latipe.store.request.CheckBalanceRequest;
import latipe.store.request.CreateStoreRequest;
import latipe.store.request.GetProvinceCodesRequest;
import latipe.store.request.MultipleStoreRequest;
import latipe.store.request.UpdateBalanceRequest;
import latipe.store.request.UpdateStoreRequest;
import latipe.store.response.ProvinceCodesResponse;
import latipe.store.response.StoreAdminResponse;
import latipe.store.response.StoreDetailResponse;
import latipe.store.response.StoreResponse;
import latipe.store.response.StoreSimplifyResponse;
import latipe.store.response.product.ProductStoreResponse;
import latipe.store.services.commission.ICommissionService;
import latipe.store.viewmodel.StoreMessage;
import lombok.AllArgsConstructor;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class StoreService implements IStoreService {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(StoreService.class);

  private final IStoreRepository storeRepository;
  private final StoreMapper storeMapper;
  private final SecureInternalProperties secureInternalProperties;
  private final RabbitMQProducer rabbitMQProducer;
  private final Gson gson;
  private final ICommissionService commissionService;
  private final MongoTemplate mongoTemplate;

  @Override
  @Async
  public CompletableFuture<StoreResponse> create(String userId, CreateStoreRequest input,
      String token) {
    UserClient userClient = Feign.builder().client(new OkHttpClient()).encoder(new GsonEncoder())
        .decoder(new GsonDecoder()).logLevel(Logger.Level.FULL).target(UserClient.class, URL);

    return CompletableFuture.supplyAsync(() -> {

      var store = storeRepository.findByOwnerId(userId).orElse(null);

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
      return storeMapper.mapToStoreResponse(store, null);
    });
  }

  @Override
  @Async
  public CompletableFuture<StoreResponse> update(String userId, UpdateStoreRequest input) {
    return CompletableFuture.supplyAsync(() -> {

      var store = storeRepository.findByOwnerId(userId)
          .orElseThrow(() -> new NotFoundException("Store not found"));

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

      return storeMapper.mapToStoreResponse(store, null);
    });
  }

  @Override
  @Async
  public CompletableFuture<String> getStoreByUserId(String userId) {
    return CompletableFuture.supplyAsync(() -> {
      var store = storeRepository.findByOwnerId(userId)
          .orElseThrow(() -> new NotFoundException("Store not found"));

      if (store == null) {
        throw new NotFoundException("Not found store");
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
      return storeMapper.mapToStoreResponse(store,
          commissionService.calcPercentStore(store.getPoint()));
    });
  }

  @Override
  public CompletableFuture<StoreDetailResponse> getMyStore(String userId) {
    return CompletableFuture.supplyAsync(() -> {
      var store = storeRepository.findByOwnerId(userId)
          .orElseThrow(() -> new NotFoundException("Store not found"));
      return storeMapper.mapToStoreDetailResponse(store,
          commissionService.calcPercentStore(store.getPoint()), store.getEWallet());
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

      var store = getStore(userId);

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
  public CompletableFuture<PagedResultDto<ProductStoreResponse>> getProductStore(long skip,
      int limit, String name, String orderBy, String storeId) {
    return CompletableFuture.supplyAsync(() -> {

      var store = storeRepository.findById(storeId)
          .orElseThrow(() -> new NotFoundException("Store not found"));

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

      var store = getStore(userId);
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

  @Override
  @Async
  public CompletableFuture<Void> checkBalance(CheckBalanceRequest input) {
    return CompletableFuture.supplyAsync(() -> {
      var store = storeRepository.findByOwnerId(input.userId())
          .orElseThrow(() -> new NotFoundException("Store not found"));

      if (!store.getIsActive() || store.getIsBan()) {
        throw new BadRequestException("Store is not active or banned");
      }

      if (store.getEWallet() < Double.parseDouble(input.amount().toString())) {
        throw new BadRequestException("Insufficient balance");
      }
      return null;
    });
  }

  @Override
  @Async
  public CompletableFuture<Void> UpdateBalance(UpdateBalanceRequest input) {
    return CompletableFuture.supplyAsync(() -> {
      var store = storeRepository.findByOwnerId(input.userId())
          .orElseThrow(() -> new NotFoundException("Store not found"));

      if (!store.getIsActive() || store.getIsBan()) {
        throw new BadRequestException("Store is not active or banned");
      }

      if (store.getEWallet() < Double.parseDouble(input.amount().toString())) {
        throw new BadRequestException("Insufficient balance");
      }

      store.setEWallet(store.getEWallet() - Double.parseDouble(input.amount().toString()));
      storeRepository.save(store);
      return null;
    });
  }

  @Async
  @Override
  public CompletableFuture<PagedResultDto<StoreAdminResponse>> getStoreAdmin(String keyword,
      Long skip,
      Integer size,
      String orderBy,
      EStatusBan ban) {
    return CompletableFuture.supplyAsync(() -> {
      Direction direction = orderBy.charAt(0) == '-' ? Direction.DESC : Direction.ASC;
      String orderByField = orderBy.charAt(0) == '-' ? orderBy.substring(1) : orderBy;

      List<Boolean> banCriteria;
      if (ban == EStatusBan.ALL) {
        banCriteria = List.of(true, false);
      } else if (ban == EStatusBan.TRUE) {
        banCriteria = List.of(true);
      } else {
        banCriteria = List.of(false);
      }

      var aggregate = Aggregation.newAggregation(StoreAdminResponse.class,
          Aggregation.match(
              Criteria.where("isBan").in(banCriteria)
                  .and("name").regex(keyword, "i")),
          Aggregation.skip(skip), Aggregation.limit(size),
          Aggregation.sort(direction, orderByField));

      var results = mongoTemplate.aggregate(aggregate, Store.class, Document.class);
      var documents = results.getMappedResults();
      var list = documents.stream()
          .map(doc -> {
            var store = gson.fromJson(doc.toJson(), StoreAdminResponse.class);
            store = StoreAdminResponse.setId(store, doc.get("_id").toString());
            return store;
          })
          .toList();

      return PagedResultDto.create(
          new Pagination(storeRepository.countStoreAdmin(banCriteria, keyword), skip, size),
          list);
    });
  }

  @Async
  @Override
  public CompletableFuture<StoreDetailResponse> getDetailStoreByAdmin(String userId) {
    return CompletableFuture.supplyAsync(() -> {
      var store = storeRepository.findById(userId)
          .orElseThrow(() -> new NotFoundException("Store not found"));
      return storeMapper.mapToStoreDetailResponse(store,
          commissionService.calcPercentStore(store.getPoint()), store.getEWallet());
    });
  }

  @Async
  @Override
  public CompletableFuture<Long> countAllStore() {
    return CompletableFuture.supplyAsync(storeRepository::count);
  }

  @Async
  @Override
  public CompletableFuture<Void> banStore(String userId, BanStoreRequest request) {
    return CompletableFuture.supplyAsync(() -> {
      var store = storeRepository.findById(userId)
          .orElseThrow(
              () -> new NotFoundException("Store not found"));
      if (store.getIsBan().equals(request.isBan())) {
        throw new BadRequestException("Store already banned");
      }
      store.setIsBan(request.isBan());
      if (request.isBan()) {
        LOGGER.info("Store {} is banned with reason {}", userId, request.reason());
        store.setReasonBan(request.reason());
      } else {
        LOGGER.info("Store {} is unbanned", userId);
        store.setReasonBan(null);
      }
      storeRepository.save(store);
      return null;
    });
  }

  @NotNull
  private Store getStore(String userId) {
    var store = storeRepository.findByOwnerId(userId)
        .orElseThrow(() -> new NotFoundException("Store not found"));

    if (store.getIsBan() != null && store.getIsBan()) {
      throw new BadRequestException("Store is banned");
    }

    if (store.getIsDeleted()) {
      throw new BadRequestException("Store is deleted");
    }
    return store;
  }
}
