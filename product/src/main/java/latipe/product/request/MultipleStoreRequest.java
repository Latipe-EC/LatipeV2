package latipe.product.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Builder;

@Builder
public record MultipleStoreRequest(@NotEmpty(message = "ids can not be empty") List<String> ids) {

}
