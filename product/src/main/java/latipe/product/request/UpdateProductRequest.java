package latipe.product.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import latipe.product.Entity.ProductClassification;
import latipe.product.Entity.ProductVariant;
import lombok.Data;

public record UpdateProductRequest (
    @NotEmpty(message = "Product Name  is required")
    String name,
    @NotEmpty(message = "Product Description  is required")
    String description,
    Double price,
    List<String> images,
    int quantity,
    List<ProductVariant> productVariant,
    List<ProductClassification> productClassifications
) {
}

