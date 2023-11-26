package latipe.payment.viewmodel;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

public record OrderMessage(
    @SerializedName(value = "order_uuid")
    String orderUuid,
    @SerializedName(value = "amount")
    BigDecimal amount,
    @SerializedName(value = "payment_method")
    Integer paymentMethod,
    @SerializedName(value = "user_request")
    UserRequest userRequest,
    Integer status

) {

}

