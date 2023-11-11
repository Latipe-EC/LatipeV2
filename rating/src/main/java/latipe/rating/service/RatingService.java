package latipe.rating.service;


import com.google.gson.Gson;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import latipe.rating.Entity.Rating;
import latipe.rating.constants.Action;
import latipe.rating.constants.Star;
import latipe.rating.dtos.PagedResultDto;
import latipe.rating.dtos.Pagination;
import latipe.rating.exceptions.BadRequestException;
import latipe.rating.exceptions.NotFoundException;
import latipe.rating.mapper.RatingMapper;
import latipe.rating.producer.RabbitMQProducer;
import latipe.rating.repositories.IRatingRepository;
import latipe.rating.request.CreateRatingRequest;
import latipe.rating.request.UpdateRatingRequest;
import latipe.rating.response.RatingResponse;
import latipe.rating.response.UserCredentialResponse;
import latipe.rating.viewmodel.RatingMessage;
import lombok.AllArgsConstructor;
import org.bson.Document;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RatingService implements IRatingService {

  private final RatingMapper ratingMapper;
  private final IRatingRepository ratingRepository;
  private final MongoTemplate mongoTemplate;
  private final RabbitMQProducer rabbitMQProducer;
  private final Gson gson;

  @Override
  @Async
  public CompletableFuture<RatingResponse> create(CreateRatingRequest request) {
    return CompletableFuture.supplyAsync(() -> {
      // CAll api check rating order

      var rating = ratingMapper.mapToRatingBeforeCreate(request);
      rating = ratingRepository.save(rating);

      String message = gson.toJson(
          RatingMessage.builder().orderItemId(rating.getOrderItemId()).ratingId(rating.getId())
              .op(Action.CREATE).build());
      rabbitMQProducer.sendMessage(message);
      return ratingMapper.mapToRatingResponse(rating);
    });
  }

  @Override
  @Async
  public CompletableFuture<RatingResponse> update(String id, UpdateRatingRequest request,
      UserCredentialResponse userCredential) {
    return CompletableFuture.supplyAsync(() -> {
      var rating = ratingRepository.findById(id)
          .orElseThrow(() -> new NotFoundException("Rating not found"));

      if (rating.getIsChange()) {
        throw new BadRequestException("You are not allowed to update this rating");
      }

      if (!rating.getUserId().equals(userCredential.id()) && !userCredential.role()
          .equals("ADMIN")) {
        throw new BadRequestException("You are not allowed to delete this rating");
      }

      ratingMapper.mapToRatingBeforeUpdate(rating, request, true);

      rating = ratingRepository.save(rating);

      String message = gson.toJson(
          RatingMessage.builder().orderItemId(rating.getOrderItemId()).ratingId(rating.getId())
              .op(Action.UPDATE).build());
      rabbitMQProducer.sendMessage(message);

      return ratingMapper.mapToRatingResponse(rating);
    });
  }

  @Override
  @Async
  public CompletableFuture<Void> delete(String id, UserCredentialResponse userCredential) {
    return CompletableFuture.supplyAsync(() -> {

      var rating = ratingRepository.findById(id)
          .orElseThrow(() -> new NotFoundException("Rating not found"));

      if (!rating.getUserId().equals(userCredential.id()) && !userCredential.role()
          .equals("ADMIN")) {
        throw new BadRequestException("You are not allowed to delete this rating");
      }

      ratingRepository.deleteById(id);

      String message = gson.toJson(
          RatingMessage.builder().orderItemId(rating.getOrderItemId()).ratingId(rating.getId())
              .op(Action.DELETE).build());
      rabbitMQProducer.sendMessage(message);

      return null;
    });
  }

  @Override
  @Async
  public CompletableFuture<PagedResultDto<RatingResponse>> getRatingProduct(String productId,
      long skip, int pageSize, String orderBy, Star filterStar) {
    return CompletableFuture.supplyAsync(() -> {
      var aggregate = createFilterByStore(skip, pageSize, orderBy, filterStar, productId, 1);

      var list = queryRating(aggregate);

      var total = ratingRepository.countRatingByProductId(productId);

      return PagedResultDto.create(Pagination.create(total, skip, pageSize), list);

    });
  }

  @Override
  @Async
  public CompletableFuture<PagedResultDto<RatingResponse>> getRatingStore(String storeId, long skip,
      int pageSize, String sortBy, Star filterStar) {
    return CompletableFuture.supplyAsync(() -> {
      var aggregate = createFilterByStore(skip, pageSize, sortBy, filterStar, storeId, 0);
      var list = queryRating(aggregate);

      var total = ratingRepository.countRatingByStoreId(storeId);

      return PagedResultDto.create(Pagination.create(total, skip, pageSize), list);

    });
  }

  @Override
  @Async
  public CompletableFuture<RatingResponse> getDetailRating(String id) {
    return CompletableFuture.supplyAsync(() -> ratingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Rating not found")))
        .thenApplyAsync(ratingMapper::mapToRatingResponse);
  }


  private TypedAggregation<RatingResponse> createFilterByStore(long skip, int limit, String orderBy,
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
            .detail(doc.getString("detail")).build()).toList();
  }

}
