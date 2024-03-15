package latipe.payment.request;


import java.math.BigDecimal;
import latipe.payment.entity.enumeration.EPaymentMethod;
import latipe.payment.entity.enumeration.EPaymentStatus;
import lombok.Builder;

@Builder
public record CapturedPaymentRequest(
    String orderId,
    String checkoutId,
    BigDecimal amount,
    Double paymentFee,
    String gatewayTransactionId,
    EPaymentMethod paymentMethod,
    EPaymentStatus paymentStatus,
    String email,
    String failureMessage) {

}