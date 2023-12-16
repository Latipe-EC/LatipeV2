package latipe.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public record LoginRequest(
    @NotEmpty(message = "is required")
    @Email(message = "should be valid")
    String username,

    @NotEmpty(message = "is required")
//    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[-+_!@#$%^&*.,?]).{6,16}$", message = "must be 6-16 characters long, with at least one special character, one lowercase letter, one uppercase letter, and one number")
    String password
) {

}
