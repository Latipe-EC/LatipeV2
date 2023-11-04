package latipe.product.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

@Builder
public record ProductStoreResponse(
    @JsonProperty(value = "id", required = true)
    String id,
    @NotEmpty(message = "Product Name  is required")
    String name,
    String image,
    int countProductVariants,
    int countSale
) {

}
