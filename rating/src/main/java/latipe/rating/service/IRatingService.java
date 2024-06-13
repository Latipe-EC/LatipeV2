package latipe.rating.service;


import jakarta.servlet.http.HttpServletRequest;
import java.util.concurrent.CompletableFuture;
import latipe.rating.constants.Star;
import latipe.rating.dtos.PagedResultDto;
import latipe.rating.request.CreateRatingRequest;
import latipe.rating.request.UpdateRatingRequest;
import latipe.rating.response.RatingResponse;

public interface IRatingService {

    CompletableFuture<RatingResponse> create(CreateRatingRequest input, HttpServletRequest request);

    CompletableFuture<RatingResponse> update(String id, UpdateRatingRequest input,
        HttpServletRequest request);

    CompletableFuture<Void> delete(String id, HttpServletRequest request);

    CompletableFuture<PagedResultDto<RatingResponse>> getRatingProduct(String productId,
        long skip, int pageSize, String sortBy, Star filterStar, HttpServletRequest request);

    CompletableFuture<PagedResultDto<RatingResponse>> getRatingStore(String storeId, long skip,
        int pageSize, String sortBy, Star filterStar, HttpServletRequest request);

    CompletableFuture<RatingResponse> getDetailRating(String id, HttpServletRequest request);

}
