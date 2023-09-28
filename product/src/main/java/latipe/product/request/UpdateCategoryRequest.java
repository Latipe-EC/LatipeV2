package latipe.product.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

public record UpdateCategoryRequest(
    @NotNull(message = "Name cannot be null")
    String name,
    String parentCategoryId,
    String image
) {


}

