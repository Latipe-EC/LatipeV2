package latipe.product.entity.product;

import jakarta.validation.constraints.Min;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
