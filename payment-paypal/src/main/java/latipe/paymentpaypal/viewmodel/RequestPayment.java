package latipe.paymentpaypal.viewmodel;

import java.math.BigDecimal;

public record RequestPayment(BigDecimal totalPrice, String checkoutId) {
}