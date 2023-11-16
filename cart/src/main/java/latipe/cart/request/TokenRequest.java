package latipe.cart.request;

import jakarta.validation.constraints.NotBlank;

public record TokenRequest(
    @NotBlank(message = "can-not-be-blank")
    String token) {

}
