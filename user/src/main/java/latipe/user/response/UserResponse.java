package latipe.user.response;

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
    List<UserAddress> addresses

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
        user.getAddresses()
    );
  }

}
