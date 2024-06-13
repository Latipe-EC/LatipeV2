package latipe.search.controllers;


import jakarta.servlet.http.HttpServletRequest;
import java.util.concurrent.CompletableFuture;
import latipe.search.annotations.ApiPrefixController;
import latipe.search.constants.ESortType;
import latipe.search.services.ProductService;
import latipe.search.viewmodel.ProductListGetVm;
import latipe.search.viewmodel.ProductNameListVm;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ApiPrefixController("/search")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/catalog-search")
    public CompletableFuture<ProductListGetVm> findProductAdvance(
        @RequestParam(defaultValue = "") String keyword,
        @RequestParam(defaultValue = "0") Integer page,
        @RequestParam(defaultValue = "12") Integer size,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String classification,
        @RequestParam(required = false) Double minPrice,
        @RequestParam(required = false) Double maxPrice,
        @RequestParam(defaultValue = "DEFAULT") ESortType sortType, HttpServletRequest request

    ) {
        return productService.findProductAdvance(keyword, page, size, category, classification,
            minPrice, maxPrice, sortType, request);
    }

    @GetMapping("/search_suggest")
    public ResponseEntity<ProductNameListVm> productSearchAutoComplete(@RequestParam String keyword,
        HttpServletRequest request
    ) {
        return ResponseEntity.ok(productService.autoCompleteProductName(keyword, request));
    }
}
