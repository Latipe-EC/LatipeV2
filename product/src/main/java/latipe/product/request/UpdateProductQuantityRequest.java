package latipe.product.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateProductQuantityRequest(
    @NotBlank(message = "Product ID cannot be blank")
    String productId,
    @NotBlank(message = "Option ID cannot be blank")
    String optionId,
    int quantity) {

}
