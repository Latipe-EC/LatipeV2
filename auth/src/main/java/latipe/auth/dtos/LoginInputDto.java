package latipe.auth.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class LoginInputDto {

  @JsonProperty(value = "username", required = true)
  @NotEmpty(message = "Username is required")
  String username;
  @NotEmpty(message = "Username is required")
  @JsonProperty(value = "password", required = true)
  String password;
}
