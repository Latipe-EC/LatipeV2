package latipe.auth.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;

public record RefreshTokenRequest(
    @NotEmpty(message = "is required")
    @JsonProperty(value = "refreshToken", required = true)
                                  String refreshToken) {

}
