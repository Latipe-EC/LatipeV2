package latipe.product.request;

import latipe.product.annotations.IsObjectId;

public record UpdateProductQuantityRequest(
    @IsObjectId
    String productId,
    @IsObjectId
    String optionId,
    int quantity) {

}
