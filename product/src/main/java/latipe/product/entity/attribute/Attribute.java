package latipe.product.entity.attribute;


import static latipe.product.constants.CONSTANTS.DATE;
import static latipe.product.constants.CONSTANTS.NUMBER;
import static latipe.product.constants.CONSTANTS.TEXT;

import latipe.product.annotations.ValuesAllow;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Attribute {

  String name;
  @ValuesAllow(values = {NUMBER, TEXT, DATE})
  String type;
  String prefixUnit;
  String options;
  Boolean isRequired;
}
