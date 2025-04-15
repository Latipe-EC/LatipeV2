package latipe.store.services.store;


import static latipe.store.utils.AuthenticationUtils.getMethodName;
import static latipe.store.utils.GenTokenInternal.generateHash;
import static latipe.store.utils.GenTokenInternal.getPrivateKey;

import com.google.gson.Gson;
import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.okhttp.OkHttpClient;
import jakarta.servlet.http.HttpServletRequest;
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
import latipe.store.response.ProvinceCodeResponse;
import latipe.store.response.ProvinceCodesResponse;
import latipe.store.response.StoreAdminResponse;
import latipe.store.response.StoreDetailResponse;
import latipe.store.response.StoreResponse;
import latipe.store.response.StoreSimplifyResponse;
import latipe.store.response.UserCredentialResponse;
import latipe.store.response.product.ProductStoreResponse;
import latipe.store.services.commission.ICommissionService;
import latipe.store.utils.GetInstanceServer;
import latipe.store.viewmodel.LogMessage;
import latipe.store.viewmodel.StoreMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service implementation for store operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StoreService implements IStoreService {

    private final IStoreRepository storeRepository;
    private final StoreMapper storeMapper;
    private final SecureInternalProperties secureInternalProperties;
    private final RabbitMQProducer rabbitMQProducer;
    private final Gson gson;
    private final ICommissionService commissionService;
    private final MongoTemplate mongoTemplate;

    private final LoadBalancerClient loadBalancer;
    private final GsonDecoder gsonDecoder;
    private final GsonEncoder gsonEncoder;
    private final OkHttpClient okHttpClient;

    @Value("${service.product}")
    private String productService;

    @Value("${service.user}")
    private String userService;

    @Value("${eureka.client.enabled}")
    private boolean useEureka;

    /**
     * Creates a new store asynchronously.
     *
     * @param input   The request containing the details for the new store.
     * @param request The HTTP servlet request to extract user context.
     * @return A CompletableFuture containing the created store's response details.
     */
    @Override
    @Async
    public CompletableFuture<StoreResponse> create(CreateStoreRequest input,
        HttpServletRequest request
    ) {

        log.info(gson.toJson(
            LogMessage.create(
                "create store with name: [%s], by user: [id: %s]".formatted(input.name(),
                    getUserId(request)), request, getMethodName())
        ));

        return CompletableFuture.supplyAsync(() -> {

            var store = storeRepository.findByOwnerId(getUserId(request)).orElse(null);

            if (store != null) {
                throw new BadRequestException("One User can only have one store");
            }

            var existingName = storeRepository.existsByName(input.name());
            if (existingName) {
                throw new BadRequestException("Store name already exists");
            }

            store = storeMapper.mapToStoreBeforeCreate(input, getUserId(request));
            store = storeRepository.save(store);

            // update role store
            var userClient = Feign.builder().client(okHttpClient).encoder(gsonEncoder)
                .decoder(gsonDecoder).target(UserClient.class,
                    useEureka ? String.format("%s/api/v1", GetInstanceServer.get(
                        loadBalancer, userService
                    )) : userService);

            userClient.upgradeVendor(request.getHeader("Authorization"));
            String message = gson.toJson(
                StoreMessage.builder().id(store.getId()).op(Action.CREATE).build());
            rabbitMQProducer.sendMessage(message);

            log.info("Create store successfully [id={}]", store.getId());
            return storeMapper.mapToStoreResponse(store, null);
        });
    }

    /**
     * Updates an existing store asynchronously.
     *
     * @param input   The request containing the updated store details.
     * @param request The HTTP servlet request to extract user context.
     * @return A CompletableFuture containing the updated store's response details.
     */
    @Override
    @Async
    public CompletableFuture<StoreResponse> update(UpdateStoreRequest input,
        HttpServletRequest request) {
        log.info(gson.toJson(
            LogMessage.create("Update store with [id: %s]".formatted(
                getUserId(request)), request, getMethodName())));

        return CompletableFuture.supplyAsync(() -> {

            var store = storeRepository.findByOwnerId(getUserId(request))
                .orElseThrow(() -> new NotFoundException("Store not found"));

            if (store.getAddress() == null) {
                throw new BadRequestException("Store address is not set");
            }

            if (store.getIsDeleted()) {
                throw new BadRequestException("Store is deleted");
            }
            if (!store.getOwnerId().equals(getUserId(request))) {
                throw new BadRequestException("You are not the owner of this store");
            }
            storeMapper.mapToStoreBeforeUpdate(store, input);
            store = storeRepository.save(store);

            log.info("Update store with [id: %s] successfully".formatted(getUserId(request)));

            return storeMapper.mapToStoreResponse(store, null);
        });
    }

    /**
     * Retrieves the store ID associated with a given user ID.
     *
     * @param userId  The ID of the user.
     * @param request The HTTP servlet request (consider extracting needed info earlier).
     * @return A CompletableFuture containing the store ID as a String.
     */
    @Override
    @Async
    public CompletableFuture<String> getStoreByUserId(
        String userId, HttpServletRequest request) {
        log.info(gson.toJson(
            LogMessage.create("Get store by user id: [%s]".formatted(
                userId), request, getMethodName())));
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
            log.info("Get store by user id: [id: %s] successfully".formatted(userId));
            return store.getId();
        });
    }

    /**
     * Retrieves detailed information for a specific store by its ID asynchronously.
     *
     * @param storeId The ID of the store to retrieve.
     * @param request The HTTP servlet request (consider extracting needed info earlier).
     * @return A CompletableFuture containing the store details.
     */
    @Override
    @Async
    public CompletableFuture<StoreResponse> getDetailStoreById(String storeId,
        HttpServletRequest request) {
        log.info("Get detail store by id {}", storeId);
        return CompletableFuture.supplyAsync(() -> {
            var store = storeRepository.findById(storeId)
                .orElseThrow(() -> new NotFoundException("Store not found"));

            if (store.getIsDeleted()) {
                throw new BadRequestException("Store is deleted");
            }
            var point = commissionService.calcPercentStore(store.getPoint(), request);

            log.info("Get detail store by id {} successfully", storeId);
            return storeMapper.mapToStoreResponse(store,
                point);
        });
    }

    /**
     * Retrieves the store details for the currently authenticated user asynchronously.
     *
     * @param request The HTTP servlet request to extract user context.
     * @return A CompletableFuture containing the user's store details.
     */
    @Override
    @Async
    public CompletableFuture<StoreDetailResponse> getMyStore(HttpServletRequest request) {
        log.info(gson.toJson(
            LogMessage.create("Get my store [userId: %s]".formatted(getUserId(request)), request,
                getMethodName())));
        return CompletableFuture.supplyAsync(() -> {
            var store = storeRepository.findByOwnerId(getUserId(request))
                .orElseThrow(() -> new NotFoundException("Store not found"));

            if (store.getIsDeleted()) {
                throw new BadRequestException("Store is deleted");
            }

            var point = commissionService.calcPercentStore(store.getPoint(), request);

            log.info("Get my store [userId: %s] successfully".formatted(getUserId(request)));
            return storeMapper.mapToStoreDetailResponse(store, point, store.getEWallet());
        });
    }

    /**
     * Retrieves province codes based on the input criteria asynchronously.
     *
     * @param input   The request containing filtering criteria.
     * @param request The HTTP servlet request (consider extracting needed info earlier).
     * @return A CompletableFuture containing the response with province codes.
     */
    @Override
    @Async
    public CompletableFuture<ProvinceCodesResponse> getProvinceCodes(GetProvinceCodesRequest input,
        HttpServletRequest request) {
        log.info(gson.toJson(
            LogMessage.create("Get province codes with [ids: %s]".formatted(
                input.ids()), request, getMethodName())));

        return CompletableFuture.supplyAsync(() -> {
            Set<String> storeIds = Set.copyOf(input.ids());
            List<String> codes = storeRepository.findByIdIn(input.ids()).stream()
                .map(x -> x.getAddress().getCityOrProvinceId()).toList();
            if (codes.size() != storeIds.size()) {
                throw new BadRequestException("Invalid province id");
            }
            log.info("Get province codes with [ids: %s] successfully".formatted(input.ids()));
            return ProvinceCodesResponse.builder().codes(codes).build();
        });
    }

    /**
     * Retrieves the province code for a specific store asynchronously.
     *
     * @param storeId The ID of the store.
     * @param request The HTTP servlet request (consider extracting needed info earlier).
     * @return A CompletableFuture containing the province code response.
     */
    @Override
    @Async
    public CompletableFuture<ProvinceCodeResponse> getProvinceCode(String storeId,
        HttpServletRequest request) {
        log.info(gson.toJson(
            LogMessage.create("Get province code with [storeId: %s]".formatted(
                storeId), request, getMethodName())));
        return CompletableFuture.supplyAsync(() -> {
            var store = storeRepository.findById(storeId)
                .orElseThrow(() -> new NotFoundException("Store not found"));

            if (store.getIsDeleted()) {
                throw new BadRequestException("Store is deleted");
            }
            log.info("Get province code with [storeId: %s] successfully".formatted(storeId));
            return new ProvinceCodeResponse(store.getAddress().getCityOrProvinceId());
        });
    }

    /**
     * Retrieves products belonging to the current user's store with pagination and filtering asynchronously.
     *
     * @param skip    The number of products to skip.
     * @param limit   The maximum number of products to return.
     * @param name    Optional filter by product name.
     * @param orderBy Optional ordering criteria.
     * @param request The HTTP servlet request to extract user context.
     * @return A CompletableFuture containing a paged result of product store responses.
     */
    @Override
    @Async
    public CompletableFuture<PagedResultDto<ProductStoreResponse>> getMyProductStore(long skip,
        int limit, String name, String orderBy, HttpServletRequest request) {
        log.info(gson.toJson(
            LogMessage.create(
                "Get my product store with [skip: %s, limit: %s, name: %s, orderBy: %s]"
                    .formatted(skip, limit, name, orderBy), request, getMethodName())));
        return CompletableFuture.supplyAsync(() -> {

            var store = getStore(getUserId(request));

            String hash;
            try {
                hash = generateHash("product-service",
                    getPrivateKey(secureInternalProperties.getPrivateKey()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            var productClient = Feign.builder().client(okHttpClient).encoder(gsonEncoder)
                .decoder(gsonDecoder).target(ProductClient.class,
                    useEureka ? String.format("%s/api/v1", GetInstanceServer.get(
                        loadBalancer, productService
                    )) : productService);

            log.info(
                "Get my product store with [skip: %s, limit: %s, name: %s, orderBy: %s] successfully"
                    .formatted(skip, limit, name, orderBy));

            return productClient.getProductStore(hash, name, skip, limit, orderBy, store.getId());
        });
    }

    @Override
    @Async
    public CompletableFuture<PagedResultDto<ProductStoreResponse>> getProductStore(long skip,
        int limit, String name, String orderBy, String storeId, HttpServletRequest request) {
        log.info(gson.toJson(
            LogMessage.create(
                "Get product store with [skip: %s, limit: %s, name: %s, orderBy: %s, storeId: %s]"
                    .formatted(skip, limit, name, orderBy, storeId), request, getMethodName())));

        return CompletableFuture.supplyAsync(() -> {

            var store = storeRepository.findById(storeId)
                .orElseThrow(() -> new NotFoundException("Store not found"));

            String hash;
            try {
                hash = generateHash("product-service",
                    getPrivateKey(secureInternalProperties.getPrivateKey()));
            } catch (Exception e) {
                log.error("Error generate hash");
                throw new RuntimeException(e);
            }

            var productClient = Feign.builder().client(okHttpClient).encoder(gsonEncoder)
                .decoder(gsonDecoder).target(ProductClient.class,
                    useEureka ? String.format("%s/api/v1", GetInstanceServer.get(
                        loadBalancer, productService
                    )) : productService);

            log.info(
                "Get product store with [skip: %s, limit: %s, name: %s, orderBy: %s, storeId: %s] successfully".formatted(
                    skip, limit, name, orderBy, storeId));
            return productClient.getProductStore(hash, name, skip, limit, orderBy, store.getId());
        });
    }

    @Override
    @Async
    public CompletableFuture<PagedResultDto<ProductStoreResponse>> getBanProductStore(long skip,
        int limit, String name, String orderBy, HttpServletRequest request) {
        log.info(gson.toJson(
            LogMessage.create(
                "Get ban product store with [skip: %s, limit: %s, name: %s, orderBy: %s]"
                    .formatted(skip, limit, name, orderBy), request, getMethodName())));

        return CompletableFuture.supplyAsync(() -> {

            var store = getStore(getUserId(request));
            String hash;
            try {
                hash = generateHash("product-service",
                    getPrivateKey(secureInternalProperties.getPrivateKey()));
            } catch (Exception e) {
                log.error("Error generate hash");
                throw new RuntimeException(e);
            }
            var productClient = Feign.builder().client(okHttpClient).encoder(gsonEncoder)
                .decoder(gsonDecoder).target(ProductClient.class,
                    useEureka ? String.format("%s/api/v1", GetInstanceServer.get(
                        loadBalancer, productService
                    )) : productService);

            log.info(
                "Get ban product store with [skip: %s, limit: %s, name: %s, orderBy: %s] successfully".formatted(
                    skip, limit, name, orderBy));
            return productClient.getBanProductStore(hash, name, skip, limit, orderBy,
                store.getId());
        });

    }

    @Override
    @Async
    public CompletableFuture<List<StoreSimplifyResponse>> getMultipleStore(
        MultipleStoreRequest input, HttpServletRequest request) {
        log.info(gson.toJson(
            LogMessage.create("Get multiple store with [ids: %s]".formatted(
                input.ids()), request, getMethodName())));

        return CompletableFuture.supplyAsync(() -> {
            var stores = storeRepository.findByIdIn(input.ids());
            if (stores.size() != input.ids().size()) {
                throw new BadRequestException("Invalid store id");
            }
            log.info("Get multiple store with [ids: %s] successfully".formatted(input.ids()));
            return stores.stream().map(storeMapper::mapToStoreSimplifyResponse).toList();
        });
    }

    @Override
    @Async
    public CompletableFuture<Void> checkBalance(CheckBalanceRequest input,
        HttpServletRequest request) {
        log.info(gson.toJson(
            LogMessage.create("Check balance with [amount: %s]".formatted(
                input.amount()), request, getMethodName())));
        return CompletableFuture.supplyAsync(() -> {
            var store = storeRepository.findByOwnerId(getUserId(request))
                .orElseThrow(() -> new NotFoundException("Store not found"));

            if (!store.getIsActive() || store.getIsBan()) {
                throw new BadRequestException("Store is not active or banned");
            }

            if (store.getEWallet() < Double.parseDouble(input.amount().toString())) {
                throw new BadRequestException("Insufficient balance");
            }
            log.info("Check balance with [amount: %s] successfully".formatted(input.amount()));
            return null;
        });
    }

    @Override
    @Async
    public CompletableFuture<Void> UpdateBalance(UpdateBalanceRequest input,
        HttpServletRequest request) {
        log.info(gson.toJson(
            LogMessage.create("Update balance with [amount: %s]".formatted(
                input.amount()), request, getMethodName())));
        return CompletableFuture.supplyAsync(() -> {
            var store = storeRepository.findByOwnerId(getUserId(request))
                .orElseThrow(() -> new NotFoundException("Store not found"));

            if (!store.getIsActive() || store.getIsBan()) {
                throw new BadRequestException("Store is not active or banned");
            }

            if (store.getEWallet() < Double.parseDouble(input.amount().toString())) {
                throw new BadRequestException("Insufficient balance");
            }

            store.setEWallet(store.getEWallet() - Double.parseDouble(input.amount().toString()));

            log.info("Update balance with [amount: %s] successfully".formatted(input.amount()));
            return null;
        });
    }

    @Async
    @Override
    public CompletableFuture<PagedResultDto<StoreAdminResponse>> getStoreAdmin(String keyword,
        Long skip,
        Integer size,
        String orderBy,
        EStatusBan ban, HttpServletRequest request) {
        log.info(gson.toJson(
            LogMessage.create(
                "Get store admin with [keyword: %s, skip: %s, size: %s, orderBy: %s, ban: %s]"
                    .formatted(keyword, skip, size, orderBy, ban), request, getMethodName())));
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

            log.info(
                "Get store admin with [keyword: %s, skip: %s, size: %s, orderBy: %s, ban: %s] successfully".formatted(
                    keyword, skip, size, orderBy, ban));
            return PagedResultDto.create(
                new Pagination(storeRepository.countStoreAdmin(banCriteria, keyword), skip, size),
                list);
        });
    }

    @Async
    @Override
    public CompletableFuture<StoreDetailResponse> getDetailStoreByAdmin(
        String userId, HttpServletRequest request) {
        log.info(gson.toJson(
            LogMessage.create("Get detail store by admin with [userId: %s]".formatted(
                userId), request, getMethodName())));
        return CompletableFuture.supplyAsync(() -> {
            var store = storeRepository.findById(getUserId(request))
                .orElseThrow(() -> new NotFoundException("Store not found"));

            var point = commissionService.calcPercentStore(store.getPoint(), request);
            log.info("Get detail store by admin with [userId: %s] successfully".formatted(userId));
            return storeMapper.mapToStoreDetailResponse(store,
                point, store.getEWallet());
        });
    }

    @Async
    @Override
    public CompletableFuture<Long> countAllStore(HttpServletRequest request) {
        log.info(gson.toJson(
            LogMessage.create("Count all store", request, getMethodName())));
        return CompletableFuture.supplyAsync(storeRepository::count);
    }

    @Async
    @Override
    public CompletableFuture<Void> banStore(
        String storeId, BanStoreRequest input, HttpServletRequest request) {
        log.info(gson.toJson(
            LogMessage.create("Ban store with [id: %s]".formatted(
                storeId), request, getMethodName())));

        return CompletableFuture.supplyAsync(() -> {
            var store = storeRepository.findById(storeId)
                .orElseThrow(
                    () -> new NotFoundException("Store not found"));
            if (store.getIsBan().equals(input.isBanned())) {
                throw new BadRequestException("Store already banned");
            }

            store.setIsBan(input.isBanned());
            if (input.isBanned()) {
                log.info("Store {} is banned with reason {}", getUserId(request), input.reason());
                store.setReasonBan(input.reason());
            } else {
                log.info("Store {} is unbanned", getUserId(request));
                store.setReasonBan(null);
            }
            storeRepository.save(store);
            log.info("Ban store with [id: %s] successfully".formatted(storeId));
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

    private String getUserId(HttpServletRequest request) {
        return ((UserCredentialResponse) request.getAttribute("user")).id();
    }
}
