package latipe.payment.viewmodel;


import latipe.payment.Entity.Payment;
import latipe.payment.Entity.enumeration.EPaymentMethod;
import latipe.payment.Entity.enumeration.EPaymentStatus;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CapturedPayment(
        Long orderId,
        String checkoutId,
        BigDecimal amount,
        Double paymentFee,
        String gatewayTransactionId,
        EPaymentMethod paymentMethod,
        EPaymentStatus paymentStatus,
        String failureMessage ) {
    public static CapturedPayment fromModel(Payment payment){
        return CapturedPayment.builder()
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