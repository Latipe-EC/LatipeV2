package latipe.store.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record MultipleStoreRequest(
    @NotEmpty(message = "ids can not be empty")
    List<String> ids) {

}
