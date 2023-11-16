package latipe.product.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import latipe.product.annotations.IsObjectId;
import latipe.product.entity.attribute.Attribute;

public record UpdateCategoryRequest(
    @NotNull(message = "Name cannot be null")
    String name,
    @IsObjectId
    String parentCategoryId,
    String image,
    String idAttributeCategory,
    List<Attribute> attributes
) {


}

