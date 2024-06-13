package latipe.rating.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.concurrent.CompletableFuture;
import latipe.rating.annotations.ApiPrefixController;
import latipe.rating.annotations.Authenticate;
import latipe.rating.constants.Star;
import latipe.rating.dtos.PagedResultDto;
import latipe.rating.request.CreateRatingRequest;
import latipe.rating.request.UpdateRatingRequest;
import latipe.rating.response.RatingResponse;
import latipe.rating.service.IRatingService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ApiPrefixController("ratings")
@AllArgsConstructor
public class RatingController {

    private final IRatingService ratingService;

    @Authenticate
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<RatingResponse> create(@Valid @RequestBody CreateRatingRequest input,
        HttpServletRequest request) {

        return ratingService.create(input, request);
    }

    @Authenticate
    @ResponseStatus(HttpStatus.OK)
    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<RatingResponse> update(@PathVariable("id") String id,
        @Valid @RequestBody UpdateRatingRequest input, HttpServletRequest request) {
        return ratingService.update(id, input, request);
    }

    @Authenticate
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<Void> delete(@PathVariable("id") String id,
        HttpServletRequest request) {
        return ratingService.delete(id, request);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<RatingResponse> getDetailRating(@PathVariable("id") String id,
        HttpServletRequest request) {
        return ratingService.getDetailRating(id, request);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "rating-product", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<PagedResultDto<RatingResponse>> getRatingProduct(
        @RequestParam String productId,
        @RequestParam(defaultValue = "0") long skip,
        @RequestParam(defaultValue = "5") int size,
        @RequestParam(defaultValue = "createdDate") String orderBy,
        Star filterStar, HttpServletRequest request) {

        return ratingService.getRatingProduct(productId, skip, size, orderBy, filterStar, request);
    }


    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "rating-store", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<PagedResultDto<RatingResponse>> getRatingStore(
        @RequestParam String storeId, @RequestParam(defaultValue = "0") long skip,
        @RequestParam(defaultValue = "5") int size,
        @RequestParam(defaultValue = "createdDate") String orderBy, Star filterStar,
        HttpServletRequest request) {

        return ratingService.getRatingStore(storeId, skip, size, orderBy, filterStar, request);
    }


}
