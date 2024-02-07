package latipe.product.viewmodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import latipe.product.request.UpdateProductQuantityRequest;

import java.util.List;

public record RollbackProductMessage(
        @SerializedName("order_id")
        String orderId,
        Integer status) {

}
