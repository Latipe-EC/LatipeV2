package latipe.product.viewmodel;

import com.google.gson.annotations.SerializedName;

public record RollbackProductMessage(
    @SerializedName("order_id")
    String orderId,
    Integer status) {

}
