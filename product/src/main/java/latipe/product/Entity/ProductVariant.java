package latipe.product.Entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariant extends AbstractAuditEntity {

  @Id
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
