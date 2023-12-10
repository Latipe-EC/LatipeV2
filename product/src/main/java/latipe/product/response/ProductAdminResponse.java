package latipe.product.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

@Builder
public record ProductAdminResponse(
    @JsonProperty(value = "id", required = true)
    String id,
    @NotEmpty(message = "Product Name  is required")
    String name,
    String image,
    int countProductVariants,
    int countSale,
    Double price,
    Double rating,
    Boolean isBanned,
    String reasonBan,
    Boolean isDeleted
) {

}
