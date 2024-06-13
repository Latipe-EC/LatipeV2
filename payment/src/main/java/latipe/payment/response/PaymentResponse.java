package latipe.payment.response;


import java.math.BigDecimal;
import latipe.payment.entity.Payment;
import latipe.payment.entity.enumeration.EPaymentMethod;
import latipe.payment.entity.enumeration.EPaymentStatus;
import lombok.Builder;

@Builder
public record PaymentResponse(
    String id,
    String orderId,
    String checkoutId,
    BigDecimal amount,
    Double paymentFee,
    String gatewayTransactionId,
    EPaymentMethod paymentMethod,
    EPaymentStatus paymentStatus,
    String failureMessage) {

    public static PaymentResponse fromModel(Payment payment) {
        return PaymentResponse.builder()
            .id(payment.getId())
            .amount(payment.getAmount())
            .paymentFee(payment.getPaymentFee())
            .checkoutId(payment.getCheckoutId())
            .orderId(payment.getOrderId())
            .gatewayTransactionId(payment.getGatewayTransactionId())
            .paymentMethod(payment.getPaymentMethod())
            .paymentStatus(payment.getPaymentStatus())
            .failureMessage(payment.getFailureMessage())
            .build();
    }
}