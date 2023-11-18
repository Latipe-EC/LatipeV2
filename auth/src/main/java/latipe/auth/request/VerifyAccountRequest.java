package latipe.auth.request;

import jakarta.validation.constraints.NotBlank;

public record VerifyAccountRequest(@NotBlank String token) {

}
