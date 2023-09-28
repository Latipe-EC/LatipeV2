package latipe.payment.response;


import java.math.BigDecimal;
import latipe.payment.Entity.Payment;
import latipe.payment.Entity.enumeration.EPaymentMethod;
import latipe.payment.Entity.enumeration.EPaymentStatus;
import lombok.Builder;

@Builder
public record CapturedPaymentResponse(
    Long orderId,
    String checkoutId,
    BigDecimal amount,
    Double paymentFee,
    String gatewayTransactionId,
    EPaymentMethod paymentMethod,
    EPaymentStatus paymentStatus,
    String failureMessage) {

  public static CapturedPaymentResponse fromModel(Payment payment) {
    return CapturedPaymentResponse.builder()
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