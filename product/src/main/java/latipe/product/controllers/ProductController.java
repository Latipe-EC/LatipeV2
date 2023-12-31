package latipe.product.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import latipe.product.annotations.ApiPrefixController;
import latipe.product.annotations.RequiresAuthorization;
import latipe.product.annotations.SecureInternalPhase;
import latipe.product.constants.EStatusBan;
import latipe.product.dtos.PagedResultDto;
import latipe.product.request.BanProductRequest;
import latipe.product.request.CreateProductRequest;
import latipe.product.request.OrderProductCheckRequest;
import latipe.product.request.ProductFeatureRequest;
import latipe.product.request.UpdateProductQuantityRequest;
import latipe.product.request.UpdateProductRequest;
import latipe.product.response.OrderProductResponse;
import latipe.product.response.ProductAdminResponse;
import latipe.product.response.ProductDetailResponse;
import latipe.product.response.ProductResponse;
import latipe.product.response.ProductStoreResponse;
import latipe.product.response.UserCredentialResponse;
import latipe.product.services.product.IProductService;
import latipe.product.viewmodel.ProductESDetailVm;
import latipe.product.viewmodel.ProductPriceVm;
import latipe.product.viewmodel.ProductThumbnailVm;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@ApiPrefixController("products")
@Validated
public class ProductController {

  private final IProductService productService;

  public ProductController(IProductService productService) {
    this.productService = productService;
  }

  @RequiresAuthorization("VENDOR")
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<ProductResponse> create(@Valid @RequestBody CreateProductRequest input) {
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    UserCredentialResponse userCredential = (UserCredentialResponse) (request.getAttribute("user"));
    return productService.create(userCredential.id(), input, request);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/get-price/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<ProductPriceVm> getPrice(@PathVariable("id") String prodId,
      @RequestParam String code) {
    return productService.getPrice(prodId, code);
  }

  @RequiresAuthorization("ADMIN")
  @GetMapping(value = "/count", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<Long> countAllProduct() {
    return productService.countAllProduct();
  }

  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/check-in-stock", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<OrderProductResponse> checkProductInStock(
      @Valid @RequestBody List<OrderProductCheckRequest> prodOrders) {
    return productService.checkProductInStock(prodOrders);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<ProductDetailResponse> getProductDetail(
      @PathVariable("id") String prodId) {
    return productService.getProductDetail(prodId);
  }

  @RequiresAuthorization("VENDOR")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{id}/advance", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<ProductResponse> getProductDetailByVendor(
      @PathVariable("id") String prodId) {
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    UserCredentialResponse userCredential = (UserCredentialResponse) (request.getAttribute("user"));
    return productService.get(userCredential.id(), prodId, request);
  }


  @RequiresAuthorization("VENDOR")
  @ResponseStatus(HttpStatus.OK)
  @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<ProductResponse> update(@PathVariable("id") String prodId,
      @Valid @RequestBody UpdateProductRequest input) {
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    UserCredentialResponse userCredential = (UserCredentialResponse) (request.getAttribute("user"));
    return productService.update(userCredential.id(), prodId, input, request);
  }

  @RequiresAuthorization("VENDOR")
  @ResponseStatus(HttpStatus.OK)
  @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<Void> delete(@PathVariable("id") String prodId) {
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    UserCredentialResponse userCredential = (UserCredentialResponse) (request.getAttribute("user"));
    return productService.remove(userCredential.id(), prodId, request);
  }

  @RequiresAuthorization("ADMIN")
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping(value = "/{id}/ban", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<Void> ban(@PathVariable("id") String prodId,
      @Valid @RequestBody BanProductRequest input) {
    return productService.ban(prodId, input);
  }

  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/list-featured", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<List<ProductThumbnailVm>> getFeatureProduct(
      @Valid @RequestBody @Size(min = 1) List<ProductFeatureRequest> products) {
    return productService.getFeatureProduct(products);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/products-es/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<ProductESDetailVm> getProductESDetailById(@PathVariable String id) {
    return productService.getProductESDetailById(id);
  }

  @RequiresAuthorization("ADMIN")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/admin", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<PagedResultDto<ProductAdminResponse>> getAdminProduct(
      @Size(max = 100)
      @RequestParam String name, @RequestParam long skip,
      @RequestParam int size, @RequestParam(defaultValue = "createdDate") String orderBy,
      @RequestParam(defaultValue = "ALL") EStatusBan statusBan) {
    return productService.getAdminProduct(skip, size, name, orderBy, statusBan);
  }

  @SecureInternalPhase
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/store/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<PagedResultDto<ProductStoreResponse>> getProductStoreInternal(
      @PathVariable String id, @RequestParam String name, @RequestParam long skip,
      @RequestParam int size, @RequestParam String orderBy) {
    return productService.getMyProductStore(skip, size, name, orderBy, id);
  }

  @SecureInternalPhase
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/store/{id}/ban", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<PagedResultDto<ProductStoreResponse>> getBanProductStoreInternal(
      @PathVariable String id, @RequestParam String
      name, @RequestParam long skip,
      @RequestParam int size, @RequestParam String orderBy) {
    return productService.getBanProductStore(skip, size, name, orderBy, id);
  }

  @SecureInternalPhase
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping(value = "/update-quantity", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<Void> updateQuantity(
      @Valid @RequestBody List<UpdateProductQuantityRequest> request) {
    return productService.updateQuantity(request);
  }
}
