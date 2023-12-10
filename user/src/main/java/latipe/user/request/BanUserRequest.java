package latipe.user.request;


import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record BanUserRequest(
    Boolean isBan,
    @Size(min = 5)
    String reason
) {


}