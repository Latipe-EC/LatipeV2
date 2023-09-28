package latipe.product.controllers;

import jakarta.validation.Valid;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import latipe.product.annotations.ApiPrefixController;
import latipe.product.annotations.RequiresAuthorization;
import latipe.product.request.BanProductRequest;
import latipe.product.request.CreateProductRequest;
import latipe.product.request.OrderProductCheckRequest;
import latipe.product.request.ProductFeatureRequest;
import latipe.product.request.UpdateProductRequest;
import latipe.product.response.OrderProductResponse;
import latipe.product.response.ProductResponse;
import latipe.product.response.UserCredentialResponse;
import latipe.product.services.product.IProductService;
import latipe.product.viewmodel.ProductESDetailVm;
import latipe.product.viewmodel.ProductPriceVm;
import latipe.product.viewmodel.ProductThumbnailVm;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestController
@ApiPrefixController("products")
public class ProductController {

    private final IProductService productService;

    public ProductController(IProductService productService) {
        this.productService = productService;
    }

    @RequiresAuthorization("VENDOR")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<ProductResponse> create(
        @Valid @RequestBody CreateProductRequest input) {
        UserCredentialResponse userCredential = ((UserCredentialResponse) (((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest()
            .getAttribute("user")));
        return productService.create(userCredential.id(), input);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/get-price/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<ProductPriceVm> getPrice(@PathVariable("id") String prodId,
        @RequestParam String code) {
        return productService.getPrice(prodId, code);
    }

    @RequiresAuthorization("VENDOR")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/check-in-stock", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<OrderProductResponse> checkProductInStock(
        @Valid @RequestBody List<OrderProductCheckRequest> prodOrders) {
        return productService.checkProductInStock(prodOrders);
    }

    @RequiresAuthorization("VENDOR")
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<ProductResponse> update(

        @PathVariable("id") String prodId, @Valid @RequestBody UpdateProductRequest input) {
        UserCredentialResponse userCredential = ((UserCredentialResponse) (((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest()
            .getAttribute("user")));
        return productService.update(userCredential.id(), prodId, input);
    }

    @RequiresAuthorization("VENDOR")
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<Void> delete(@PathVariable("id") String prodId) {
        UserCredentialResponse userCredential = ((UserCredentialResponse) (((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest()
            .getAttribute("user")));
        return productService.remove(userCredential.id(), prodId);
    }

    @RequiresAuthorization("ADMIN")
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(value = "/ban/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<Void> ban(
        @PathVariable("id") String prodId,
        @Valid @RequestBody BanProductRequest input) {
        UserCredentialResponse userCredential = ((UserCredentialResponse) (((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest()
            .getAttribute("user")));
        return productService.ban(prodId, input);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/list-featured", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<List<ProductThumbnailVm>> getFeatureProduct(
        List<ProductFeatureRequest> products) {
        return productService.getFeatureProduct(products);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/products-es/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<ProductESDetailVm> getProductESDetailById(@PathVariable String id) {
        return productService.getProductESDetailById(id);
    }
}
