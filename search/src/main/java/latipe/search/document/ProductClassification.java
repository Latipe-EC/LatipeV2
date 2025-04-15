package latipe.search.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a product classification within the search service.
 * Product classifications define variants of products with different attributes,
 * such as size, color, or other properties.
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductClassification {

    /**
     * Unique identifier for the product classification
     */
    String id;
    
    /**
     * Image URL representing this product classification
     */
    String image;
    
    /**
     * Name of the product classification
     */
    String name;
    
    /**
     * Available quantity in stock
     */
    int quantity;
    
    /**
     * Regular price of the product classification
     */
    double price;
    
    /**
     * Promotional price (when on sale)
     */
    double promotionalPrice;
    
    /**
     * Stock Keeping Unit identifier
     */
    String sku;
    
    /**
     * Custom code for the product classification
     */
    String code;
}