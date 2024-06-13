package latipe.payment.response;

import java.math.BigDecimal;
import latipe.payment.entity.Payment;
import latipe.payment.entity.enumeration.EPaymentMethod;
import latipe.payment.entity.enumeration.EPaymentStatus;

public record CheckPaymentOrderResponse(
    String checkoutId,
    BigDecimal amount,
    Double paymentFee,
    EPaymentMethod paymentMethod,
    EPaymentStatus paymentStatus
) {


    public static CheckPaymentOrderResponse fromModel(Payment payment) {
        return new CheckPaymentOrderResponse(
            payment.getCheckoutId(),
            payment.getAmount(),
            payment.getPaymentFee(),
            payment.getPaymentMethod(),
            payment.getPaymentStatus()
        );
    }
}
