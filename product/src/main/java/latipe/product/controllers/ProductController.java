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
import latipe.product.request.ProductESDetailsRequest;
import latipe.product.request.ProductFeatureRequest;
import latipe.product.request.UpdateProductQuantityRequest;
import latipe.product.request.UpdateProductRequest;
import latipe.product.response.OrderProductResponse;
import latipe.product.response.ProductAdminResponse;
import latipe.product.response.ProductDetailResponse;
import latipe.product.response.ProductListGetResponse;
import latipe.product.response.ProductNameListResponse;
import latipe.product.response.ProductResponse;
import latipe.product.response.ProductSIEResponse;
import latipe.product.response.ProductStoreResponse;
import latipe.product.services.product.IProductService;
import latipe.product.viewmodel.ProductESDetailVm;
import latipe.product.viewmodel.ProductPriceVm;
import latipe.product.viewmodel.ProductThumbnailVm;
import lombok.RequiredArgsConstructor;
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

@RestController
@ApiPrefixController("products")
@Validated
@RequiredArgsConstructor
public class ProductController {

    private final IProductService productService;

    @RequiresAuthorization("VENDOR")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<ProductResponse> create(@Valid @RequestBody CreateProductRequest input,
        HttpServletRequest request) {
        return productService.create(input, request);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/get-price/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<ProductPriceVm> getPrice(@PathVariable("id") String prodId,
        @RequestParam String code, HttpServletRequest request) {
        return productService.getPrice(prodId, code, request);
    }

    @RequiresAuthorization("ADMIN")
    @GetMapping(value = "/count", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<Long> countAllProduct(HttpServletRequest request) {
        return productService.countAllProduct(request);
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/check-in-stock", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<OrderProductResponse> checkProductInStock(
        @Valid @RequestBody List<OrderProductCheckRequest> prodOrders, HttpServletRequest request) {
        return productService.checkProductInStock(prodOrders, request);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<ProductDetailResponse> getProductDetail(
        @PathVariable("id") String prodId, HttpServletRequest request) {
        return productService.getProductDetail(prodId, request);
    }

    @RequiresAuthorization("VENDOR")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{id}/advance", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<ProductResponse> getProductDetailByVendor(
        @PathVariable("id") String prodId, HttpServletRequest request) {
        return productService.get(prodId, request);
    }


    @RequiresAuthorization("VENDOR")
    @ResponseStatus(HttpStatus.OK)
    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<ProductResponse> update(@PathVariable("id") String prodId,
        @Valid @RequestBody UpdateProductRequest input, HttpServletRequest request) {
        return productService.update(prodId, input, request);
    }

    @RequiresAuthorization("VENDOR")
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<Void> delete(@PathVariable("id") String prodId,
        HttpServletRequest request) {
        return productService.remove(prodId, request);
    }

    @RequiresAuthorization("ADMIN")
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(value = "/{id}/ban", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<Void> ban(@PathVariable("id") String prodId,
        @Valid @RequestBody BanProductRequest input, HttpServletRequest request) {
        return productService.ban(prodId, input, request);
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/list-featured", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<List<ProductThumbnailVm>> getFeatureProduct(
        @Valid @RequestBody @Size(min = 1) List<ProductFeatureRequest> products,
        HttpServletRequest request) {
        return productService.getFeatureProduct(products, request);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/products-es/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<ProductESDetailVm> getProductESDetailById(@PathVariable String id,
        HttpServletRequest request) {
        return productService.getProductESDetailById(id, request);
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/products-es-multiple", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<List<ProductSIEResponse>> getProductESDetailForAI(
        @RequestBody ProductESDetailsRequest input,
        HttpServletRequest request) {
        return productService.getProductESDetails(input, request);
    }

    @RequiresAuthorization("ADMIN")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/admin", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<PagedResultDto<ProductAdminResponse>> getAdminProduct(
        @Size(max = 100)
        @RequestParam String name, @RequestParam long skip,
        @RequestParam int size, @RequestParam(defaultValue = "createdDate") String orderBy,
        @RequestParam(defaultValue = "ALL") EStatusBan statusBan, HttpServletRequest request) {
        return productService.getAdminProduct(skip, size, name, orderBy, statusBan, request);
    }

    @SecureInternalPhase
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/store/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<PagedResultDto<ProductStoreResponse>> getProductStoreInternal(
        @PathVariable String id, @RequestParam String name, @RequestParam long skip,
        @RequestParam int size, @RequestParam String orderBy, HttpServletRequest request) {
        return productService.getMyProductStore(skip, size, name, orderBy, id, request);
    }

    @SecureInternalPhase
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/store/{id}/ban", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<PagedResultDto<ProductStoreResponse>> getBanProductStoreInternal(
        @PathVariable String id, @RequestParam String
        name, @RequestParam long skip,
        @RequestParam int size, @RequestParam String orderBy, HttpServletRequest request) {
        return productService.getBanProductStore(skip, size, name, orderBy, id, request);
    }

    @SecureInternalPhase
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(value = "/update-quantity", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<Void> updateQuantity(
        @Valid @RequestBody List<UpdateProductQuantityRequest> input, HttpServletRequest request) {
        return productService.updateQuantity(input, request);
    }


    //
    @GetMapping("/catalog-search")
    public CompletableFuture<ProductListGetResponse> findProductAdvance(
        @RequestParam(defaultValue = "") String keyword,
        @RequestParam(defaultValue = "0") Integer page,
        @RequestParam(defaultValue = "12") Integer size,
        @RequestParam(required = false) String category
        , HttpServletRequest request) {
        return productService.findProductAdvance(keyword, page, size, category, request);
    }

    @GetMapping("/search_suggest")
    public CompletableFuture<ProductNameListResponse> autoCompleteProductName(
        @RequestParam String keyword, HttpServletRequest request) {
        return productService.autoCompleteProductName(keyword, request);
    }
}
