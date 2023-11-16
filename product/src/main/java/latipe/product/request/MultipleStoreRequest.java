package latipe.product.request;

import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Builder;

@Builder
public record MultipleStoreRequest(
    List<@Size(min = 1) String> ids) {

}
