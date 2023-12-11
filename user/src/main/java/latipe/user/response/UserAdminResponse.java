package latipe.user.response;

import java.util.Date;
import lombok.Builder;

@Builder
public record UserAdminResponse(
    String id,
    String displayName,
    String phoneNumber,
    String email,
    String avatar,
    String role,
    Double eWallet,
    Integer point,
    String username,
    Boolean isBanned,
    Boolean isDeleted,
    String reasonBan,
    String gender,
    Date birthday
) {

}
