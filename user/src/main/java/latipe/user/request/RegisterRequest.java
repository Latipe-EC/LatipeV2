package latipe.user.request;

import java.util.Date;
import latipe.user.entity.Gender;

public record RegisterRequest(

    String firstName,

    String lastName,

    String phoneNumber,

    String email,

    String hashedPassword,

    String avatar,
    Date birthday,
    Gender gender) {

}
