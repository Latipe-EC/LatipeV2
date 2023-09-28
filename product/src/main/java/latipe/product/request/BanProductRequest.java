package latipe.product.request;

import jakarta.validation.constraints.Min;
import lombok.Data;

public record BanProductRequest(@Min(5)
                                String reason) {

}
