package latipe.product.response;

import lombok.Builder;

@Builder
public record UserCredentialResponse(
    String id,
    String email,
    String phone,
    String role
) {

}
