package latipe.product.entity.product;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UsingItem {
    private String productId;
    private String optionId;
    private Integer quantity;
}
