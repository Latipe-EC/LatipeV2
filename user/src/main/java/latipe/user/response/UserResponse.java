package latipe.user.response;

import java.time.ZonedDateTime;
import java.util.List;
import latipe.user.entity.User;
import latipe.user.entity.UserAddress;

public record UserResponse(
    String id,
    String firstName,
    String lastName,
    String displayName,
    String phoneNumber,
    String email,
    String avatar,
    String role,
    Double eWallet,
    Integer point,
    String username,
    Boolean isChangeUsername,
    List<UserAddress> addresses,
    String gender,
    ZonedDateTime birthday

) {

  public static UserResponse fromUser(User user) {
    return new UserResponse(
        user.getId(),
        user.getFirstName(),
        user.getLastName(),
        user.getDisplayName(),
        user.getPhoneNumber(),
        user.getEmail(),
        user.getAvatar(),
        user.getRole().getName(),
        user.getEWallet(),
        user.getPoint(),
        user.getUsernameReal(),
        user.getIsChangeUsername(),
        user.getAddresses(),
        user.getGender().name(),
        user.getBirthday()
    );
  }

}
