package latipe.payment.viewmodel;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

public record OrderMessage(
    @SerializedName(value = "order_id")
    String orderId,
    @SerializedName(value = "amount")
    BigDecimal amount,
    @SerializedName(value = "payment_method")
    Integer paymentMethod,
    @SerializedName(value = "user_id")
    String userId,
    Integer status
) {

}

