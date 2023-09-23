package latipe.user.services.user.Dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class UserUpdateDto extends  UserCreateDto{
  @JsonIgnore
  public String hashedPassword;
  @Override
  public String getHashedPassword() {
    return null;
  }
}
