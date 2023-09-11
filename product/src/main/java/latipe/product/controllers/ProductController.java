package latipe.product.controllers;

import jakarta.validation.Valid;
import latipe.product.annotations.ApiPrefixController;
import latipe.product.annotations.RequiresAuthorization;
import latipe.product.dtos.ProductPriceDto;
import latipe.product.dtos.UserCredentialDto;
import latipe.product.services.product.Dtos.*;
import latipe.product.services.product.IProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
    public CompletableFuture<ProductDto> create(@RequestAttribute(value = "user") UserCredentialDto userCredential,
                                                @Valid @RequestBody ProductCreateDto input) {
        return productService.create(userCredential.getId(), input);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/get-price/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<ProductPriceDto> getPrice(@PathVariable("id") String prodId, @RequestParam  String code)
    {
        return productService.getPrice(prodId, code);
    }
    @RequiresAuthorization("VENDOR")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/check-in-stock", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<OrderProductResultsDto> checkProductInStock(@Valid @RequestBody List<OrderProductCheckDto> prodOrders){
        return productService.checkProductInStock(prodOrders);
    }

    @RequiresAuthorization("VENDOR")
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<ProductDto> update(@RequestAttribute(value = "user") UserCredentialDto userCredential,
                                                @PathVariable("id") String prodId, @Valid @RequestBody ProductUpdateDto input) throws InvocationTargetException, IllegalAccessException {
        return productService.update(userCredential.getId(), prodId, input);
    }

    @RequiresAuthorization("VENDOR")
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<Void> delete(@RequestAttribute(value = "user") UserCredentialDto userCredential,
                                                @PathVariable("id") String prodId) {
        return productService.remove(userCredential.getId(), prodId );
    }

    @RequiresAuthorization("ADMIN")
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(value = "/ban/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<Void> ban(@RequestAttribute(value = "user") UserCredentialDto userCredential,
                                          @Valid @RequestBody BanProductDto input) {
        return productService.ban(userCredential.getId(), input );
    }
}
