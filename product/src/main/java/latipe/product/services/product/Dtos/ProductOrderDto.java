package latipe.product.services.product.Dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductOrderDto {
    String productId;
    String name;
    int quantity;
    double price;
    String optionId;
    String nameOption = null;
    Double totalPrice = null;
}
