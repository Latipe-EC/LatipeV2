package latipe.auth.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import latipe.auth.entity.Gender;

public record RegisterRequest(
    @NotBlank(
        message = "First Name  is required")
    String firstName,

    @NotBlank(message = "Last Name  is required")
    String lastName,

    @NotBlank(message = "Phone is mandatory")
    @Pattern(regexp = "^\\d{10,11}$", message = "Phone have 10 to 11 digit")
    @JsonProperty(value = "phoneNumber")
    String phoneNumber,

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email should be valid")
    @JsonProperty(value = "email")
    String email,

    @NotBlank(message = "password is mandatory")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[-+_!@#$%^&*.,?]).{6,16}$", message = "Password must be 6-16 characters long, with at least one special character, one lowercase letter, one uppercase letter, and one number")
    String hashedPassword,

    @JsonProperty(value = "avatar")
    String avatar,

    Gender gender,
    String birthday
) {

}
