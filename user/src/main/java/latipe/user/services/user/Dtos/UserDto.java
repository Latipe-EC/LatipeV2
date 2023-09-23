package latipe.user.services.user.Dtos;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import latipe.user.Entity.Role;
import lombok.Data;

@Data
public class UserDto extends UserUpdateDto {

  private String id;
}
