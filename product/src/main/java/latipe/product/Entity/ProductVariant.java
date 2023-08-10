package latipe.product.Entity;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariant extends AbstractAuditEntity {
    String id;
    String name;
    String image;
    List<Options> options;
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Options {
        String value;
    }
}
