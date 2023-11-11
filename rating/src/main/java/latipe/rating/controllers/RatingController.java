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
import latipe.rating.response.UserCredentialResponse;
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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestController
@ApiPrefixController("ratings")
@AllArgsConstructor
public class RatingController {

  private final IRatingService ratingService;

  @Authenticate
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<RatingResponse> create(@Valid @RequestBody CreateRatingRequest request) {
    return ratingService.create(request);
  }

  @Authenticate
  @ResponseStatus(HttpStatus.OK)
  @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<RatingResponse> update(@PathVariable("id") String id,
      @Valid @RequestBody UpdateRatingRequest input) {
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    UserCredentialResponse userCredential = (UserCredentialResponse) (request.getAttribute("user"));

    return ratingService.update(id, input, userCredential);
  }

  @Authenticate
  @ResponseStatus(HttpStatus.OK)
  @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<Void> delete(@PathVariable("id") String id) {
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    UserCredentialResponse userCredential = (UserCredentialResponse) (request.getAttribute("user"));
    return ratingService.delete(id, userCredential);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<RatingResponse> getDetailRating(@PathVariable("id") String id) {
    return ratingService.getDetailRating(id);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "rating-product", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<PagedResultDto<RatingResponse>> getRatingProduct(
      @RequestParam String productId, @RequestParam long skip, @RequestParam int size,
      @RequestParam String orderBy, Star filterStar) {

    return ratingService.getRatingProduct(productId, skip, size, orderBy, filterStar);
  }


  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "rating-store", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<PagedResultDto<RatingResponse>> getRatingStore(
      @RequestParam String storeId, @RequestParam long skip, @RequestParam int size,
      @RequestParam String orderBy, Star filterStar) {

    return ratingService.getRatingStore(storeId, skip, size, orderBy, filterStar);
  }


}
