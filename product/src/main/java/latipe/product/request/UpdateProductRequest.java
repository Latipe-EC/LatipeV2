package latipe.product.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import latipe.product.viewmodel.ProductClassificationVm;
import latipe.product.viewmodel.ProductVariantVm;

public record UpdateProductRequest(
    @NotEmpty(message = "Product Name  is required")
    String name,
    @NotEmpty(message = "Product Description  is required")
    String description,
    Double price,
    List<String> images,
    int quantity,
    List<ProductVariantVm> productVariant,
    List<ProductClassificationVm> productClassifications
) {

}

