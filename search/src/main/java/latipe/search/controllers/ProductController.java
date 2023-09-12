package latipe.search.controllers;


import latipe.search.constants.ESortType;
import latipe.search.services.ProductService;
import latipe.search.viewmodel.ProductListGetVm;
import latipe.search.viewmodel.ProductNameListVm;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
public class ProductController {
    private final ProductService productService;
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/storefront/catalog-search")
    public CompletableFuture<ProductListGetVm> findProductAdvance(@RequestParam(defaultValue = "") String keyword,
                                                                  @RequestParam(defaultValue = "0") Integer page,
                                                                  @RequestParam(defaultValue = "12") Integer size,
                                                                  @RequestParam(required = false) String category,
                                                                  @RequestParam(required = false) String classification,
                                                                  @RequestParam(required = false) Double minPrice,
                                                                  @RequestParam(required = false) Double maxPrice,
                                                                  @RequestParam(defaultValue = "DEFAULT") ESortType sortType) {
        return productService.findProductAdvance(keyword, page, size, category, classification, minPrice, maxPrice, sortType);
    }

    @GetMapping("/storefront/search_suggest")
    public ResponseEntity<ProductNameListVm> productSearchAutoComplete(@RequestParam String keyword) {
        return ResponseEntity.ok(productService.autoCompleteProductName(keyword));
    }
}
