package latipe.user.request;

import java.time.ZonedDateTime;
import latipe.user.entity.Gender;

public record RegisterRequest(

    String firstName,

    String lastName,

    String phoneNumber,

    String email,

    String hashedPassword,

    String avatar,
    ZonedDateTime birthday,
    Gender gender) {

}
