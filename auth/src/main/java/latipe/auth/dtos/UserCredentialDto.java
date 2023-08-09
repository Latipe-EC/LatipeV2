package latipe.auth.dtos;

import lombok.Builder;

@Builder
public class UserCredentialDto {
    private String id;
    private String email;
    private String phone;
    private String role;
}
