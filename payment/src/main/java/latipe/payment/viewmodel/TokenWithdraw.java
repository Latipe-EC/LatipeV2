package latipe.payment.viewmodel;

import java.time.ZonedDateTime;

public record TokenWithdraw(
    String id,
    ZonedDateTime createdAt
) {

}
