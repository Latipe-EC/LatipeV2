package latipe.product.viewmodel;

import java.util.List;

public record ProductVariantVm(
    String name,
    String image,
    List<String> options
) {

}
