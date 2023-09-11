package latipe.product.services.product.Dtos;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class BanProductDto {
    @Min(5)
    String reason;
}
