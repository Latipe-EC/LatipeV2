package latipe.product.viewmodel;

import java.util.List;
import org.springframework.data.annotation.Id;

public record ProductVariantVm(
    String name,
    String image,
    List<String> options
    ) {

}
