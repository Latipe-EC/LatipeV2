package latipe.user.response;

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
    Boolean isBan
) {

}
