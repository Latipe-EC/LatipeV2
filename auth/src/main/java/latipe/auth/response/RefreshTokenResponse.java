package latipe.auth.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RefreshTokenResponse(@JsonProperty(value = "accessToken", required = true)
                                   String accessToken,
                                   @JsonProperty(value = "refreshToken", required = true)
                                   String refreshToken
) {

}
