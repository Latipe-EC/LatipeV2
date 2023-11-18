package latipe.user.request;

public record RegisterRequest(

    String firstName,

    String lastName,

    String phoneNumber,

    String email,

    String hashedPassword,

    String avatar) {

}
