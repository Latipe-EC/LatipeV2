package latipe.store.request;

import java.math.BigDecimal;

public record UpdateBalanceRequest(
    String userId,
    BigDecimal amount
) {

}
