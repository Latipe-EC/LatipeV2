package latipe.product.request;

import jakarta.validation.constraints.NotBlank;

public record ProductFeatureRequest(@NotBlank(message = "can-not-be-blank") String productId,
                                    @NotBlank(message = "can-not-be-blank") String optionId) {

}
