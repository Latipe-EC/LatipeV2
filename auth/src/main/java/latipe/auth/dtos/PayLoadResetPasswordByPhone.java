package latipe.auth.dtos;

import lombok.Data;

@Data
public class PayLoadResetPasswordByPhone {
    String newPassword;
    String token;
}
