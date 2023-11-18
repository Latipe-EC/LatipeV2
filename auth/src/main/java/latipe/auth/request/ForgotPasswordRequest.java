package latipe.auth.request;

import jakarta.validation.constraints.Email;

public record ForgotPasswordRequest(
    @Email(message = "should be valid")
    String email
) {

}
