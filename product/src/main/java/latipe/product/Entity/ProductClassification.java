package latipe.product.Entity;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class ProductClassification {
    String id;
    String image;
    int quantity;
    double price;
    String sku;
    String code;
}
