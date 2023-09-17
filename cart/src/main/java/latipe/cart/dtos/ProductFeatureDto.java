package latipe.cart.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductFeatureDto {
    String productId;
    String optionId;
}
