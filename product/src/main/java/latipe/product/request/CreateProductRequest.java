package latipe.product.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import latipe.product.viewmodel.ProductClassificationVm;
import latipe.product.viewmodel.ProductVariantVm;

public record CreateProductRequest(
    @NotEmpty(message = "Product Name  is required")
    String name,
    @NotEmpty(message = "Product Description  is required")
    String description,
    Double price,
    @Size(min = 1, message = "Product Categories is required")
    @NotBlank(message = "Product Categories is required")
    List<String> categories,
    List<String> images,
    int quantity,
    List<ProductVariantVm> productVariants,
    Boolean isPublished,
    List<ProductClassificationVm> productClassifications) {

}
