package latipe.auth.response;

import java.time.LocalDateTime;
import latipe.auth.constants.CONSTANTS.TOKEN_TYPE;
import lombok.Builder;

@Builder
public record TokenResetPasswordResponse(
    String token,
    String email,
    LocalDateTime expired,
    TOKEN_TYPE type
) {
  public TokenResetPasswordResponse withExpired(LocalDateTime expired) {
    return new TokenResetPasswordResponse(token, email, expired, type);
  }
}
