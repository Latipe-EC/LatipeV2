package latipe.cart.request;

import latipe.cart.annotations.IsObjectId;

public record ProductFeatureRequest(
    @IsObjectId
    String productId,
    @IsObjectId
    String optionId) {

}
