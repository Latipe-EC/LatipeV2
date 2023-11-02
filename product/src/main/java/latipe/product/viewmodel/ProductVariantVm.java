package latipe.product.viewmodel;

import java.util.List;
import latipe.product.entity.product.Options;

public record ProductVariantVm(
    String name,
    String image,
    List<Options> options
) {

}
