package latipe.auth.response;

import java.util.List;
import latipe.auth.viewmodel.UserAddress;

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
    List<UserAddress> addresses,
    String username,
    Boolean isChangeUsername,
    String gender,
    String birthday
) {


}
