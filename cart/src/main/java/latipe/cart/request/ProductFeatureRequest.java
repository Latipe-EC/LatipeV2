package latipe.cart.request;

import jakarta.validation.constraints.NotEmpty;

public record ProductFeatureRequest(
    @NotEmpty(message = "Product id is required")
    String productId,
    @NotEmpty(message = "Option id is required")
    String optionId) {

}
