package latipe.payment.viewmodel;

import com.google.gson.annotations.SerializedName;

public record UserRequest(
    @SerializedName(value = "user_id")
    String userId
) {

}
