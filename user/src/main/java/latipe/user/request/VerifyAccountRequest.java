package latipe.user.request;

import jakarta.validation.constraints.NotBlank;

public record VerifyAccountRequest(@NotBlank String token) {

}
