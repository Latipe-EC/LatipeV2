package latipe.product.Entity;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Variants {
    String mainVariant;
    @Min(1)
    List<Options> options = new ArrayList<>();
    List<subVariants> subVariants = new ArrayList<>();
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class subVariants {
        @Min(0)
        String subVariant;
        @Min(1)
        List<Options> options;
    }
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Options {
        String name;
        String image;
        @Min(0)
        Double price;
        @Min(0)
        int quantity;
        String sku;
    }
}
