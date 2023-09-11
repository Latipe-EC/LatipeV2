package latipe.product.services.product.Dtos;

import lombok.Data;

@Data
public class OrderProductCheckDto {
    String productId;
    String optionId;
    int quantity;
}
