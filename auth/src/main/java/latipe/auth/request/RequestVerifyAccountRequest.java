package latipe.auth.request;

import jakarta.validation.constraints.NotBlank;

public record RequestVerifyAccountRequest(@NotBlank String email) {

}
