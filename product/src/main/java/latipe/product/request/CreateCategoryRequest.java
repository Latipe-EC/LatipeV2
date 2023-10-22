package latipe.product.request;

import jakarta.validation.constraints.NotNull;

public record CreateCategoryRequest(
    @NotNull(message = "Name cannot be null")
    String name,
    String parentCategoryId,
    String image,
    String idAttributeCategory
) {

}