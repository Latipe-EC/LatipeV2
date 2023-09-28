package latipe.product.request;

import jakarta.validation.constraints.Min;

public record BanProductRequest(@Min(5)
                                String reason) {

}
