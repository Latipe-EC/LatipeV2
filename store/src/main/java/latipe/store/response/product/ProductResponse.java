package latipe.store.response.product;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ProductResponse(@JsonProperty(value = "id", required = true) String id,
                              @NotEmpty(message = "Product Name  is required") String name,
                              @NotEmpty(message = "Product Description  is required") String description,
                              Double price, Double promotionalPrice, List<String> images,
                              List<CategoryResponse> categories, int quantity,
                              List<ProductVariant> productVariants,
                              List<ProductClassification> productClassifications,
                              @JsonProperty(value = "isDeleted") Boolean isDeleted,
                              boolean isBanned, boolean isPublished, int countSale,
                              List<AttributeValue> detailsProduct) {

}
