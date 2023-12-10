package latipe.user.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.Date;
import latipe.user.entity.Gender;

public record CreateUserRequest(
    @NotBlank(
        message = "First Name  is required")
    String firstName,

    @NotBlank(message = "Last Name  is required")
    String lastName,

    @Pattern(regexp = "^\\d{10,11}$", message = "Phone have 10 to 11 digit")
    String phoneNumber,

    @Email(message = "Email should be valid")
    String email,

    String avatar,
    String role,
    Date birthday,
    Gender gender
) {


}
