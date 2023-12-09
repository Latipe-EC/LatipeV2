package latipe.payment.request;

import java.math.BigDecimal;

public record CheckBalanceStoreRequest(
    String userId,
    BigDecimal amount) {

}
