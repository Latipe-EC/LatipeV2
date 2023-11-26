package latipe.payment.viewmodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record OrderMessage(
    @JsonProperty(value = "order_uuid")
    String orderUuid,
    @JsonProperty(value = "amount")
    BigDecimal amount,
    @JsonProperty(value = "payment_method")
    Integer paymentMethod,
    @JsonProperty(value = "user_request")
    UserRequest userRequest,
    Integer status

) {

}
