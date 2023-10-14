package latipe.product.entity.attribute;


import static latipe.product.constants.CONSTANTS.DATE;
import static latipe.product.constants.CONSTANTS.NUMBER;
import static latipe.product.constants.CONSTANTS.TEXT;

import latipe.product.annotations.ValuesAllow;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttributeValue {

  String name;
  String value;
  Boolean required;
  String prefixUnit;

  @ValuesAllow(values = {NUMBER, TEXT, DATE})
  String typeValue;
}
