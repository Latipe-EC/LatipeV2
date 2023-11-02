package latipe.product.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import latipe.product.entity.attribute.AttributeValue;
import latipe.product.entity.product.ProductClassification;
import latipe.product.entity.product.ProductVariant;

public record ProductResponse(
    @JsonProperty(value = "id", required = true)
    String id,
    @NotEmpty(message = "Product Name  is required")
    String name,
    @NotEmpty(message = "Product Description  is required")
    String description,
    Double price,
    Double promotionalPrice,
    List<String> images,
    List<CategoryResponse> categories,
    int quantity,
    List<ProductVariant> productVariants,
    List<ProductClassification> productClassifications,
    @JsonProperty(value = "isDeleted")
    Boolean isDeleted,
    boolean isBanned,
    boolean isPublished,
    int countSale,
    List<AttributeValue> detailsProduct
) {

}
