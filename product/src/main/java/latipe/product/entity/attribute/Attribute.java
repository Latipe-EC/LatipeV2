package latipe.product.entity.attribute;


import static latipe.product.constants.CONSTANTS.DATE;
import static latipe.product.constants.CONSTANTS.NUMBER;
import static latipe.product.constants.CONSTANTS.TEXT;

import latipe.product.annotations.ValuesAllow;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a product attribute.
 * This class defines attributes that can be associated with products,
 * including the attribute name, type, and other metadata.
 */
@Getter
@Setter
public class Attribute {

    /**
     * The name of the attribute
     */
    String name;
    
    /**
     * The data type of the attribute. Must be one of: NUMBER, TEXT, or DATE
     */
    @ValuesAllow(values = {NUMBER, TEXT, DATE})
    String type;
    
    /**
     * Optional unit prefix for the attribute value (e.g., "kg", "$", etc.)
     */
    String prefixUnit;
    
    /**
     * Available options for attribute values, typically used for selection fields
     */
    String options;
    
    /**
     * Indicates whether this attribute must have a value
     */
    Boolean isRequired;
}
