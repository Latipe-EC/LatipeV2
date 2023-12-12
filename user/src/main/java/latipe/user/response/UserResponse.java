package latipe.user.response;

import java.text.SimpleDateFormat;
import java.util.List;
import latipe.user.entity.Gender;
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
    String birthday

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
        user.getGender() != null ? user.getGender().name() : Gender.OTHER.name(),
        new SimpleDateFormat("yyyy-MM-dd").format(
            user.getBirthday() != null ? user.getBirthday() : new java.util.Date())
    );
  }

}
