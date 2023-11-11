package latipe.rating.service;


import java.util.concurrent.CompletableFuture;
import latipe.rating.dtos.PagedResultDto;
import latipe.rating.request.CreateRatingRequest;
import latipe.rating.request.UpdateRatingRequest;
import latipe.rating.response.RatingResponse;

public interface IRatingService {
  public CompletableFuture<RatingResponse> create(CreateRatingRequest request);
  public CompletableFuture<RatingResponse> update(UpdateRatingRequest request);
  public CompletableFuture<PagedResultDto<RatingResponse>> getRatingProduct(String id);
  public CompletableFuture<PagedResultDto<RatingResponse>> getRatingStore(String storeId);
  public CompletableFuture<RatingResponse> getDetailRating(String id);

}
