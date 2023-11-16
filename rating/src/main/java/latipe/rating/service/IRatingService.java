package latipe.rating.service;


import java.util.concurrent.CompletableFuture;
import latipe.rating.constants.Star;
import latipe.rating.dtos.PagedResultDto;
import latipe.rating.request.CreateRatingRequest;
import latipe.rating.request.UpdateRatingRequest;
import latipe.rating.response.RatingResponse;
import latipe.rating.response.UserCredentialResponse;

public interface IRatingService {

  CompletableFuture<RatingResponse> create(CreateRatingRequest request);

  CompletableFuture<RatingResponse> update(String id, UpdateRatingRequest request,
      UserCredentialResponse userCredential);

  CompletableFuture<Void> delete(String id, UserCredentialResponse userCredential);

  CompletableFuture<PagedResultDto<RatingResponse>> getRatingProduct(String productId,
      long skip, int pageSize, String sortBy, Star filterStar);

  CompletableFuture<PagedResultDto<RatingResponse>> getRatingStore(String storeId, long skip,
      int pageSize, String sortBy, Star filterStar);

  CompletableFuture<RatingResponse> getDetailRating(String id);

}
