package latipe.payment.request;


import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record CancelOrderRequest(
    String userId,
    BigDecimal money
) {


}