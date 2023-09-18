package latipe.payment.dtos;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class UserCredentialDto {
    private String id;
    private String email;
    private String phone;
    private String role;
}
