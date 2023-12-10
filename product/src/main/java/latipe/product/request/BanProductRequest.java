package latipe.product.request;

import jakarta.validation.constraints.Size;

public record BanProductRequest(
    Boolean isBanned,

    @Size(min = 5)
    String reason) {

}
