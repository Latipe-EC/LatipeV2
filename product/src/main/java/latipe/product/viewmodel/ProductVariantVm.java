package latipe.product.viewmodel;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import latipe.product.entity.product.Options;

public record ProductVariantVm(
    @NotBlank(message = "Product Variant Name is required")
    String name,
    String image,
    List<Options> options
) {

}
