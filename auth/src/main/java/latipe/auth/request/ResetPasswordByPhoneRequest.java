package latipe.auth.request;

public record ResetPasswordByPhoneRequest(String newPassword,
                                          String token) {

}
