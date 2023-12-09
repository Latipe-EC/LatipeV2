package latipe.payment.request;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record WithdrawPaypalRequest(
    @Email(message = "should be valid")
    String email,
    @DecimalMin(value = "0.0", inclusive = false, message = "must be greater than 0")
    BigDecimal amount
) {


}