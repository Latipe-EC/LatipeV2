package latipe.product.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.Set;
import lombok.Builder;

@Builder
public record MultipleStoreRequest(@NotEmpty(message = "ids can not be empty") Set<String> ids) {

}
