package latipe.user.request;

import java.math.BigDecimal;

public record CheckBalanceRequest(String userId,
                                  BigDecimal money) {

}
