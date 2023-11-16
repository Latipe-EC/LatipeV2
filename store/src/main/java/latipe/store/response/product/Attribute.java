package latipe.store.response.product;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Attribute {

  String name;
  String type;
  String prefixUnit;
  String options;
  Boolean isRequired;
}
