package latipe.payment.request;

import java.math.BigDecimal;

public record UpdateBalanceStoreRequest(
    String userId,
    BigDecimal amount) {

}
