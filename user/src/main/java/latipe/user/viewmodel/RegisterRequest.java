package latipe.user.viewmodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
public record RegisterRequest(@NotEmpty(message = "First Name  is required")
                       String firstName,

                       @NotEmpty(message = "Last Name  is required")
                       String lastName,

                       @Pattern(regexp = "^\\d{10,11}$", message = "Phone have 10 to 11 digit")
                       @JsonProperty(value = "phoneNumber")
                       String phoneNumber,

                       @NotEmpty(message = "Email is mandatory")
                       @Email(message = "Email should be valid")
                       @JsonProperty(value = "email")
                       String email,

                       @NotEmpty(message = "password is mandatory")
                       @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[-+_!@#$%^&*.,?]).{6,16}$", message = "Password must be 6-16 characters long, with at least one special character, one lowercase letter, one uppercase letter, and one number")
                       @JsonProperty(value = "hashedPassword")
                       String hashedPassword,

                       @JsonProperty(value = "avatar")
                       String avatar) {

}
