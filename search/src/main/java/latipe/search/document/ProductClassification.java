package latipe.search.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductClassification {

    String id;
    String image;
    String name;
    int quantity;
    double price;
    double promotionalPrice;
    String sku;
    String code;
}