package latipe.product.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import latipe.product.entity.attribute.AttributeValue;
import latipe.product.viewmodel.ProductClassificationVm;
import latipe.product.viewmodel.ProductVariantVm;

public record UpdateProductRequest(@NotEmpty(message = "Product Name  is required") String name,
                                   @NotEmpty(message = "Product Description  is required") String description,
                                   List<String> categories, Double price, Double promotionalPrice,
                                   List<String> images, int quantity,
                                   List<ProductVariantVm> productVariants,
                                   List<ProductClassificationVm> productClassifications,
                                   List<AttributeValue> detailsProduct,
                                   Boolean isPublished) {

}

