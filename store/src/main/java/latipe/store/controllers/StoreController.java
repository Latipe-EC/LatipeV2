package latipe.store.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import latipe.store.annotations.ApiPrefixController;
import latipe.store.annotations.Authenticate;
import latipe.store.annotations.RequiresAuthorization;
import latipe.store.annotations.SecureInternalPhase;
import latipe.store.constants.EStatusBan;
import latipe.store.dtos.PagedResultDto;
import latipe.store.request.BanStoreRequest;
import latipe.store.request.CheckBalanceRequest;
import latipe.store.request.CreateStoreRequest;
import latipe.store.request.GetProvinceCodesRequest;
import latipe.store.request.MultipleStoreRequest;
import latipe.store.request.UpdateBalanceRequest;
import latipe.store.request.UpdateStoreRequest;
import latipe.store.response.ProvinceCodesResponse;
import latipe.store.response.StoreAdminResponse;
import latipe.store.response.StoreDetailResponse;
import latipe.store.response.StoreResponse;
import latipe.store.response.StoreSimplifyResponse;
import latipe.store.response.UserCredentialResponse;
import latipe.store.response.product.ProductStoreResponse;
import latipe.store.services.store.IStoreService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
@ApiPrefixController("stores")
@Validated
public class StoreController {

  private final IStoreService storeService;

  public StoreController(IStoreService storeService) {
    this.storeService = storeService;
  }

  @Authenticate
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/validate-store/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<String> validateStore(@PathVariable String userId) {
    return storeService.getStoreByUserId(userId);
  }

  @RequiresAuthorization("ADMIN")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{storeId}/admin", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<StoreDetailResponse> getDetailStoreByAdmin(
      @PathVariable String storeId) {
    return storeService.getDetailStoreByAdmin(storeId);
  }

  @RequiresAuthorization("ADMIN")
  @GetMapping(value = "/count", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<Long> countAllStore() {
    return storeService.countAllStore();
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{storeId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<StoreResponse> getDetailStore(@PathVariable String storeId) {
    return storeService.getDetailStoreById(storeId);
  }

  @Authenticate
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/register-store", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<StoreResponse> createStore(@RequestBody CreateStoreRequest input) {

    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    String token = request.getHeader("Authorization");
    UserCredentialResponse userCredential = (UserCredentialResponse) (request.getAttribute("user"));
    return storeService.create(userCredential.id(), input, token);

  }

  @RequiresAuthorization("VENDOR")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/my", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<StoreDetailResponse> getMyStore() {
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    UserCredentialResponse userCredential = (UserCredentialResponse) (request.getAttribute("user"));
    return storeService.getMyStore(userCredential.id());
  }

  @RequiresAuthorization("VENDOR")
  @ResponseStatus(HttpStatus.OK)
  @PutMapping(value = "/my", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<StoreResponse> updateStore(@RequestBody UpdateStoreRequest input)
      throws InvocationTargetException, IllegalAccessException {
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    UserCredentialResponse userCredential = (UserCredentialResponse) (request.getAttribute("user"));
    return storeService.update(userCredential.id(), input);

  }

  @Authenticate
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/my-products", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<PagedResultDto<ProductStoreResponse>> getMyProductStore(
      @RequestParam(value = "skip", defaultValue = "0") long skip,
      @RequestParam(value = "limit", defaultValue = "10") int limit,
      @RequestParam(value = "name", defaultValue = "") String name,
      @RequestParam(value = "orderBy", defaultValue = "createdDate") String orderBy) {
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    UserCredentialResponse userCredential = (UserCredentialResponse) (request.getAttribute("user"));
    return storeService.getMyProductStore(skip, limit, name, orderBy, userCredential.id());

  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{storeId}/products", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<PagedResultDto<ProductStoreResponse>> getProductStore(
      @PathVariable String storeId,
      @RequestParam(value = "skip", defaultValue = "0") long skip,
      @RequestParam(value = "limit", defaultValue = "10") int limit,
      @RequestParam(value = "name", defaultValue = "") String name,
      @RequestParam(value = "orderBy", defaultValue = "createdDate") String orderBy) {
    return storeService.getProductStore(skip, limit, name, orderBy, storeId);

  }

  @Authenticate
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/my-products/ban", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<PagedResultDto<ProductStoreResponse>> getMyProductBanStore(
      @RequestParam(value = "skip", defaultValue = "0") long skip,
      @RequestParam(value = "limit", defaultValue = "10") int limit,
      @RequestParam(value = "name", defaultValue = "") String name,
      @RequestParam(value = "orderBy", defaultValue = "createdDate") String orderBy) {
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    UserCredentialResponse userCredential = (UserCredentialResponse) (request.getAttribute("user"));
    return storeService.getBanProductStore(skip, limit, name, orderBy, userCredential.id());
  }

  @SecureInternalPhase
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/get-province-codes", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<ProvinceCodesResponse> getProvinceCodes(
      @RequestBody GetProvinceCodesRequest input) {
    return storeService.getProvinceCodes(input);
  }

  @SecureInternalPhase
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/multiple-detail-store", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<List<StoreSimplifyResponse>> getMultipleStore(
      @Valid @RequestBody MultipleStoreRequest input) {
    return storeService.getMultipleStore(input);

  }

  @SecureInternalPhase
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/check-balance", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<Void> checkBalance(
      @Valid @RequestBody CheckBalanceRequest input
  ) {
    return storeService.checkBalance(input);

  }

  @SecureInternalPhase
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping(value = "/balance", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<Void> updateBalance(
      @Valid @RequestBody UpdateBalanceRequest input
  ) {
    return storeService.UpdateBalance(input);

  }

  @RequiresAuthorization("ADMIN")
  @GetMapping(value = "/admin", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<PagedResultDto<StoreAdminResponse>> getUserAdmin(
      @RequestParam(defaultValue = "") String keyword,
      @RequestParam(defaultValue = "0") Long skip,
      @RequestParam(defaultValue = "12") Integer size,
      @RequestParam(defaultValue = "createdDate") String orderBy,
      @RequestParam(defaultValue = "ALL") EStatusBan isBan) {
    return storeService.getStoreAdmin(keyword, skip, size, orderBy, isBan);
  }

  @RequiresAuthorization("ADMIN")
  @PatchMapping(value = "/{userId}/ban", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<Void> banStore(
      @PathVariable String userId,
      @Valid @RequestBody BanStoreRequest request) {
    return storeService.banStore(userId, request);
  }
}
