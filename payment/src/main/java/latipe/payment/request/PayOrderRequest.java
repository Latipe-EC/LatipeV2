package latipe.payment.request;


import java.math.BigDecimal;
import latipe.payment.Entity.Payment;
import latipe.payment.Entity.enumeration.EPaymentMethod;
import latipe.payment.Entity.enumeration.EPaymentStatus;
import lombok.Builder;

@Builder
public record PayOrderRequest(
    String orderId
) {


}