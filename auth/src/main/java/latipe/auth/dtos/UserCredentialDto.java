package latipe.auth.dtos;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UserCredentialDto {
    private String id;
    private String email;
    private String phone;
    private String role;
}
