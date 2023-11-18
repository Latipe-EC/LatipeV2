package latipe.user.viewmodel;

public record ForgotPasswordMessage(
    String name,
    String email,
    String token
) {

}
