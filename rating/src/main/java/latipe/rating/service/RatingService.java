package latipe.rating.service;


import java.util.concurrent.CompletableFuture;
import latipe.rating.constants.Star;
import latipe.rating.dtos.PagedResultDto;
import latipe.rating.mapper.RatingMapper;
import latipe.rating.repositories.IRatingRepository;
import latipe.rating.request.CreateRatingRequest;
import latipe.rating.request.UpdateRatingRequest;
import latipe.rating.response.RatingResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RatingService implements IRatingService {

  private final RatingMapper ratingMapper;
  private final IRatingRepository ratingRepository;

  @Override
  public CompletableFuture<RatingResponse> create(CreateRatingRequest request) {
    return null;
  }

  @Override
  public CompletableFuture<RatingResponse> update(UpdateRatingRequest request) {
    return null;
  }

  @Override
  public CompletableFuture<PagedResultDto<RatingResponse>> getRatingProduct(String productId,
      long skip, int pageSize, String sortBy, Star filterStar) {
    return null;
  }

  @Override
  public CompletableFuture<PagedResultDto<RatingResponse>> getRatingStore(String storeId,
      long skip, int pageSize, String sortBy, Star filterStar) {
    return CompletableFuture.supplyAsync(() -> {
      var ratings = ratingRepository.findByStoreId(storeId);
      return new PagedResultDto<>(ratings.stream().map(ratingMapper::mapToRatingResponse).toList(),
          ratings.size());
    });
  }

  @Override
  public CompletableFuture<RatingResponse> getDetailRating(String id) {
    return CompletableFuture.supplyAsync(() -> ratingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Rating not found")))
        .thenApplyAsync(ratingMapper::mapToRatingResponse);
  }
}
