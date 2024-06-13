package latipe.rating.service;


import static latipe.rating.constants.CONSTANTS.X_API_KEY_ORDER;
import static latipe.rating.utils.AuthenticationUtils.getMethodName;
import static latipe.rating.utils.GenTokenInternal.generateHash;
import static latipe.rating.utils.GenTokenInternal.getPrivateKey;

import com.google.gson.Gson;
import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.okhttp.OkHttpClient;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import latipe.rating.configs.SecureInternalProperties;
import latipe.rating.constants.Action;
import latipe.rating.constants.Star;
import latipe.rating.dtos.PagedResultDto;
import latipe.rating.dtos.Pagination;
import latipe.rating.entity.Rating;
import latipe.rating.exceptions.BadRequestException;
import latipe.rating.exceptions.NotFoundException;
import latipe.rating.feign.OrderClient;
import latipe.rating.feign.UserClient;
import latipe.rating.mapper.RatingMapper;
import latipe.rating.producer.RabbitMQProducer;
import latipe.rating.repositories.IRatingRepository;
import latipe.rating.request.CreateRatingRequest;
import latipe.rating.request.UpdateRatingRequest;
import latipe.rating.response.RatingResponse;
import latipe.rating.response.UserCredentialResponse;
import latipe.rating.utils.GetInstanceServer;
import latipe.rating.viewmodel.LogMessage;
import latipe.rating.viewmodel.RatingMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RatingService implements IRatingService {

    private final RatingMapper ratingMapper;
    private final IRatingRepository ratingRepository;
    private final MongoTemplate mongoTemplate;
    private final RabbitMQProducer rabbitMQProducer;
    private final SecureInternalProperties secureInternalProperties;
    private final Gson gson;
    private final OrderClient orderClient;

    private final LoadBalancerClient loadBalancer;
    private final GsonDecoder gsonDecoder;
    private final GsonEncoder gsonEncoder;
    private final OkHttpClient okHttpClient;

    @Value("${service.user}")
    private String userService;

    @Value("${eureka.client.enabled}")
    private boolean useEureka;

    @Override
    @Async
    public CompletableFuture<RatingResponse> create(CreateRatingRequest input,
        HttpServletRequest request) {
        log.info(gson.toJson(LogMessage.create("Create rating by user with id: %s".formatted(
            getUserId(request)), request, getMethodName())));

        return CompletableFuture.supplyAsync(() -> {
            // CAll api check rating order
            // TODO : REMEMBER CHANGE TO REAL TOKEN
            var response = orderClient.getRating(X_API_KEY_ORDER, input.orderItemId());
            if (response.getData().getRating_id()
                != null && !response.getData().getRating_id()
                .isBlank()) {
                throw new BadRequestException("rating already exist");
            }

            // get info user

            String hash;
            try {
                hash = generateHash("user-service",
                    getPrivateKey(secureInternalProperties.getPrivateKey()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            var userClient = Feign.builder().client(okHttpClient).encoder(gsonEncoder)
                .decoder(gsonDecoder).target(UserClient.class,
                    useEureka ? String.format("%s/api/v1", GetInstanceServer.get(
                        loadBalancer, userService
                    )) : userService);

            var userDetail = userClient.getInfoForRating(hash, getUserId(request));

            var rating = ratingMapper.mapToRatingBeforeCreate(input, getUserId(request),
                userDetail.username(),
                userDetail.avatar());
            rating = ratingRepository.save(rating);

            var message = gson.toJson(
                RatingMessage.builder().orderItemId(rating.getOrderItemId())
                    .ratingId(rating.getId())
                    .storeId(rating.getStoreId())
                    .rating(rating.getRating()).productId(rating.getProductId()).op(Action.CREATE)
                    .build());
            rabbitMQProducer.sendMessage(message);

            log.info("Create rating successfully");
            return ratingMapper.mapToRatingResponse(rating);
        });
    }

    @Override
    @Async
    public CompletableFuture<RatingResponse> update(String id, UpdateRatingRequest input,
        HttpServletRequest request) {
        log.info(gson.toJson(
            LogMessage.create("Update rating with id: %s".formatted(id), request,
                getMethodName())));
        return CompletableFuture.supplyAsync(() -> {
            var rating = ratingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Rating not found"));

            var oldRating = rating.getRating();
            if (rating.getIsChange()) {
                throw new BadRequestException("You are not allowed to update this rating");
            }

            if (!rating.getUserId().equals(getUserId(request))
                && !((UserCredentialResponse) request.getAttribute("user")).role()
                .equals("ADMIN")) {
                throw new BadRequestException("You are not allowed to delete this rating");
            }

            ratingMapper.mapToRatingBeforeUpdate(rating, input, true);

            rating = ratingRepository.save(rating);

            var message = gson.toJson(
                RatingMessage.builder().orderItemId(rating.getOrderItemId())
                    .ratingId(rating.getId())
                    .rating(rating.getRating()).productId(rating.getProductId()).op(Action.UPDATE)
                    .oldRating(oldRating).storeId(rating.getStoreId())
                    .build());
            rabbitMQProducer.sendMessage(message);

            log.info("Update rating successfully");
            return ratingMapper.mapToRatingResponse(rating);
        });
    }

    @Override
    @Async
    public CompletableFuture<Void> delete(String id, HttpServletRequest request) {
        log.info(gson.toJson(
            LogMessage.create("Delete rating with id: %s".formatted(id), request,
                getMethodName())));
        return CompletableFuture.supplyAsync(() -> {

            var rating = ratingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Rating not found"));

            if (!rating.getUserId().equals(getUserId(request))
                && !((UserCredentialResponse) request.getAttribute("user")).role()
                .equals("ADMIN")) {
                throw new BadRequestException("You are not allowed to delete this rating");
            }

            ratingRepository.deleteById(id);

            String message = gson.toJson(
                RatingMessage.builder().orderItemId(rating.getOrderItemId()).ratingId(null)
                    .rating(rating.getRating()).productId(rating.getProductId()).op(Action.DELETE)
                    .storeId(rating.getStoreId())
                    .build());
            rabbitMQProducer.sendMessage(message);
            log.info("Delete rating successfully");
            return null;
        });
    }

    @Override
    @Async
    public CompletableFuture<PagedResultDto<RatingResponse>> getRatingProduct(String productId,
        long skip, int pageSize, String orderBy, Star filterStar, HttpServletRequest request) {
        log.info(gson.toJson(
            LogMessage.create("Get rating by product with id: %s".formatted(productId), request,
                getMethodName())));
        return CompletableFuture.supplyAsync(() -> {
            var aggregate = createFilterByStore(skip, pageSize, orderBy, filterStar, productId, 1);

            var list = queryRating(aggregate);

            var total = ratingRepository.countRatingByProductId(productId);

            log.info("Get rating by product successfully");
            return PagedResultDto.create(Pagination.create(total, skip, pageSize), list);

        });
    }

    @Override
    @Async
    public CompletableFuture<PagedResultDto<RatingResponse>> getRatingStore(String storeId,
        long skip,
        int pageSize, String sortBy, Star filterStar, HttpServletRequest request) {
        log.info(gson.toJson(
            LogMessage.create("Get rating by store with id: %s".formatted(storeId), request,
                getMethodName())));
        return CompletableFuture.supplyAsync(() -> {
            var aggregate = createFilterByStore(skip, pageSize, sortBy, filterStar, storeId, 0);
            var list = queryRating(aggregate);

            var total = ratingRepository.countRatingByStoreId(storeId);
            log.info("Get rating by store successfully");
            return PagedResultDto.create(Pagination.create(total, skip, pageSize), list);
        });
    }

    @Override
    @Async
    public CompletableFuture<RatingResponse> getDetailRating(String id,
        HttpServletRequest request) {
        log.info(
            gson.toJson(LogMessage.create("Get detail rating with id: %s".formatted(id), request,
                getMethodName())));

        var rating = ratingRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Rating not found"));

        log.info("Get detail rating successfully");
        return CompletableFuture.supplyAsync(() -> rating)
            .thenApplyAsync(ratingMapper::mapToRatingResponse);
    }


    private TypedAggregation<RatingResponse> createFilterByStore(long skip, int limit,
        String orderBy,
        Star filterStar, String id, int option) {
        Direction direction = orderBy.charAt(0) == '-' ? Direction.DESC : Direction.ASC;
        String orderByField = orderBy.charAt(0) == '-' ? orderBy.substring(1) : orderBy;

        Criteria criteriaStar = new Criteria();
        if (filterStar != Star.ALL) {
            criteriaStar.and("rating").is(filterStar.ordinal());
        }

        return Aggregation.newAggregation(RatingResponse.class,
            Aggregation.match(Criteria.where(option == 0 ? "storeId" : "productId").is(id)),
            Aggregation.match(criteriaStar), Aggregation.skip(skip), Aggregation.limit(limit),
            Aggregation.sort(direction, orderByField));
    }


    private List<RatingResponse> queryRating(TypedAggregation<RatingResponse> aggregate) {
        var results = mongoTemplate.aggregate(aggregate, Rating.class, Document.class);
        var documents = results.getMappedResults();
        return documents.stream().map(
            doc -> RatingResponse.builder().id(doc.getObjectId("_id").toString())
                .content(doc.getString("content")).rating(doc.getInteger("rating"))
                .userId(doc.getString("userId")).userName(doc.getString("userName"))
                .productId(doc.getString("productId")).storeId(doc.getString("storeId"))
                .createdDate(doc.getDate("created_date")).isChange(doc.getBoolean("isChange"))
                .detail(doc.getString("detail")).build()).toList();
    }

    private String getUserId(HttpServletRequest request) {
        return ((UserCredentialResponse) request.getAttribute("user")).id();
    }
}
