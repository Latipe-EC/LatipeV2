package latipe.payment.request;


import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record ValidWithdrawPaypalRequest(
    @NotBlank(message = "should not be blank")
    String token
) {


}