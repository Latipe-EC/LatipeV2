package latipe.auth.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LoginDto {
    @JsonProperty(value = "accessToken", required = true)
    public String accessToken;
    @JsonProperty(value = "refreshToken", required = true)
    public String refreshToken;
    @JsonProperty(value = "user", required = true)
    public UserProfileDto user;
    public LoginDto(String accessToken, String refreshToken, UserProfileDto user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = user;
    }
}
