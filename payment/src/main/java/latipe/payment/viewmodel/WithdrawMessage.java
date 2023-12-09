package latipe.payment.viewmodel;

import java.math.BigDecimal;

public record WithdrawMessage(
    String email,
    BigDecimal amount,
    String withdrawId,
    String emailRecipient,
    String type,
    Long expiryDate
) {

}
