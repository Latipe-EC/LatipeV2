package latipe.store.request;

import java.math.BigDecimal;

public record CheckBalanceRequest(
    String userId,
    BigDecimal amount
) {

}
