package latipe.search.document;

import lombok.*;

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
    String sku;
    String code;
}