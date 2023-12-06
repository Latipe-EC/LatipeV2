package latipe.user.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserNameRequest(
    @NotBlank(message = "Username is mandatory")
    String username) {

}
