package latipe.product.viewmodel;

import java.util.List;

public record ProductSample(String id, String image, ProductClassificationVm productClassifications,
                            List<ProductVariantVm> productVariants) {

}
