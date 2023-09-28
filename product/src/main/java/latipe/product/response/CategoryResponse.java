package latipe.product.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record CategoryResponse(
    @JsonProperty(value = "id", required = true)
    String id,
    @NotNull(message = "Name cannot be null")
    String name,
    String parentCategoryId,
    String image,
    @JsonProperty(value = "isDeleted")
    Boolean isDeleted
) {

}

