package latipe.user.services.user.Dtos;


import lombok.Data;

@Data
public class UserDto extends UserUpdateDto{
    @Override
    public String getHashedPassword() {
        return null;
    }
}
