package latipe.user.request;

public record ResetPasswordRequest(
    String token,
    String password

) {

}
