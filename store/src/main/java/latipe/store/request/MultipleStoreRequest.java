package latipe.store.request;

import jakarta.validation.constraints.Size;
import java.util.List;

public record MultipleStoreRequest(
    List<@Size(min = 1) String> ids) {

}
