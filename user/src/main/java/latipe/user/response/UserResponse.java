package latipe.user.response;

import latipe.user.Entity.User;

public record UserResponse(
    String id,
    String firstName,
    String lastName,
    String displayName,
    String phoneNumber,
    String email,
    String hashedPassword,
    String avatar,
    String role,
    Double eWallet,
    Integer point

) {
  public static UserResponse fromUser(User user) {
    return new UserResponse(
        user.getId(),
        user.getFirstName(),
        user.getLastName(),
        user.getDisplayName(),
        user.getPhoneNumber(),
        user.getEmail(),
        user.getHashedPassword(),
        user.getAvatar(),
        user.getRole().getName(),
        user.getEWallet(),
        user.getPoint()
    );
  }

}
