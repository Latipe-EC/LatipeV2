package latipe.product.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import latipe.product.entity.attribute.AttributeValue;
import latipe.product.viewmodel.ProductClassificationVm;
import latipe.product.viewmodel.ProductVariantVm;

public record CreateProductRequest(
    @NotEmpty(message = "Product Name  is required")
    String name,
    @NotEmpty(message = "Product Description  is required")
    String description,
    Double price,
    Double promotionalPrice,
    @Size(min = 1, message = "Product Categories is required")
    @NotEmpty(message = "Product Categories is required")
    List<String> categories,
    List<String> images,
    int quantity,
    List<@Valid ProductVariantVm> productVariants,
    Boolean isPublished,
    List<@Valid ProductClassificationVm> productClassifications,
    List<AttributeValue> detailsProduct) {

}
