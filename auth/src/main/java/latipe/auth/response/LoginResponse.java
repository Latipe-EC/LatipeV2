package latipe.auth.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import lombok.Builder;

@Builder
public record LoginResponse(@JsonProperty(value = "accessToken", required = true)
                            String accessToken,
                            @JsonProperty(value = "refreshToken", required = true)
                            String refreshToken,
                            @JsonProperty(value = "id")
                            String id,
                            @JsonProperty(value = "firstName")
                            String firstName,
                            @JsonProperty(value = "lastName")
                            String lastName,
                            @JsonProperty(value = "displayName")
                            String displayName,
                            @JsonProperty(value = "phone")
                            String phone,
                            @JsonProperty(value = "email")
                            String email,
                            @JsonProperty(value = "bio")
                            String bio,
                            String role,
                            @JsonProperty(value = "lastActiveAt")
                            Date lastActiveAt) {

}
