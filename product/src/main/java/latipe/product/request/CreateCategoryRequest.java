package latipe.product.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

public record CreateCategoryRequest(
    @NotNull(message = "Name cannot be null")
    String name,
    String parentCategoryId,
    String image
) {

}