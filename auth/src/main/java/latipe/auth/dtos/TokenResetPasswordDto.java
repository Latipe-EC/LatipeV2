package latipe.auth.dtos;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TokenResetPasswordDto {
    private String token;
    private String email;
    private LocalDateTime expired;
    private int type;

    public TokenResetPasswordDto(String token, String userEmail, LocalDateTime expiryDate, int i) {
        this.token = token;
        this.email = userEmail;
        this.expired = expiryDate;
        this.type = i;
    }

    public TokenResetPasswordDto() {

    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expired);
    }
}
