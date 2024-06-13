package latipe.payment.response;


import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record TotalAmountResponse(
    BigDecimal amount
) {

}