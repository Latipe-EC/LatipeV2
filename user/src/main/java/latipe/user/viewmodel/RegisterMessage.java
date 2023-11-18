package latipe.user.viewmodel;

public record RegisterMessage(
    String type,
    String id,
    String name,
    String email,
    String password,
    String token
) {

}
