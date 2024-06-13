package latipe.product.viewmodel;

import com.google.gson.annotations.SerializedName;

public record OrderReplyMessage(
    int status,
    @SerializedName(value = "order_id")
    String orderId
) {

    public static OrderReplyMessage create(int status, String orderId) {
        return new OrderReplyMessage(status, orderId);
    }

}
