package latipe.product.services.product.Dtos;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OrderProductResultsDto {
    List<ProductOrderDto> products;
    Double totalPrice;
}
