package latipe.user.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public record UpdateUserRequest(
    @NotEmpty(
        message = "First Name  is required")
    String firstName,
    @NotEmpty(
        message = "Last Name is required")
    String lastName,
    String displayName,
    @Pattern(regexp = "^\\d{10,11}$", message = "Phone have 10 to 11 digit")
    @JsonProperty(value = "phoneNumber")
    String phoneNumber,
    String avatar
) {

}
