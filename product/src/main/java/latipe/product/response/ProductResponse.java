package latipe.product.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import latipe.product.Entity.ProductClassification;
import latipe.product.Entity.ProductVariant;

public record ProductResponse(
    @JsonProperty(value = "id", required = true)
    String id,
    @NotEmpty(message = "Product Name  is required")
    String name,
    @NotEmpty(message = "Product Description  is required")
    String description,
    Double price,
    List<String> images,
    int quantity,
    List<ProductVariant> productVariant,
    List<ProductClassification> productClassifications,
    @JsonProperty(value = "isDeleted")
    Boolean isDeleted,
    boolean isBanned,
    boolean isPublished
) {

}
