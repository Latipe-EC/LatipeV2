package latipe.payment.request;


import java.math.BigDecimal;
import latipe.payment.entity.Payment;
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
    String failureMessage) {

  public static CapturedPaymentRequest fromModel(Payment payment) {
    return CapturedPaymentRequest.builder()
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