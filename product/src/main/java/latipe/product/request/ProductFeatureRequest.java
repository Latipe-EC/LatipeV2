package latipe.product.request;

import latipe.product.annotations.IsObjectId;

public record ProductFeatureRequest(
    @IsObjectId String productId,

    @IsObjectId String optionId) {

}
