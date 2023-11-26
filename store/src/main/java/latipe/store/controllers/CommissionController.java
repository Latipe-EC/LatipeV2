package latipe.store.controllers;

import jakarta.validation.Valid;
import java.util.concurrent.CompletableFuture;
import latipe.store.annotations.ApiPrefixController;
import latipe.store.annotations.RequiresAuthorization;
import latipe.store.request.CreateCommissionRequest;
import latipe.store.request.UpdateCommissionRequest;
import latipe.store.response.CommissionResponse;
import latipe.store.services.commission.ICommissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ApiPrefixController("commissions")
@Validated
@RequiredArgsConstructor
public class CommissionController {

  private final ICommissionService commissionService;

  @RequiresAuthorization("ADMIN")
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<CommissionResponse> create(
      @RequestBody @Valid
      CreateCommissionRequest request
  ) {
    return commissionService.create(request);
  }

  @RequiresAuthorization("ADMIN")
  @ResponseStatus(HttpStatus.OK)
  @PutMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<CommissionResponse> update(
      @PathVariable String id,
      @RequestBody @Valid
      UpdateCommissionRequest request
  ) {
    return commissionService.update(id, request);
  }

  @RequiresAuthorization("ADMIN")
  @ResponseStatus(HttpStatus.OK)
  @DeleteMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<Void> delete(
      @PathVariable String id
  ) {
    return commissionService.delete(id);
  }

//  @ResponseStatus(HttpStatus.OK)
//  @GetMapping(value = "/calc-commission-store/{storeId}",
//      produces = MediaType.APPLICATION_JSON_VALUE)
//  public CompletableFuture<CalcPercentResponse> calc(
//      @PathVariable String storeId
//  ) {
//    return commissionService.calcPercentStore(storeId);
//  }
}
