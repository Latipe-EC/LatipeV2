package latipe.auth.request;

import jakarta.validation.constraints.NotEmpty;

public record TokenRequest (
    @NotEmpty(message = "is required")
    String token){

}
