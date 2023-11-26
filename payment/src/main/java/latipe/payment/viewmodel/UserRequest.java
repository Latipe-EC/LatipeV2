package latipe.payment.viewmodel;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserRequest(
    @JsonProperty(value = "user_id")
    String userId
) {

}
