package latipe.product.Entity;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductClassification {
    String id;
    String image;
    String name;
    int quantity;
    double price;
    String sku;
    String code;
}
