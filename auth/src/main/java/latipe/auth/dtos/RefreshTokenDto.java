package latipe.auth.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RefreshTokenDto {
    @JsonProperty(value = "accessToken", required = true)
    public String accessToken;
    @JsonProperty(value = "refreshToken", required = true)
    public String refreshToken;

    public RefreshTokenDto(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
