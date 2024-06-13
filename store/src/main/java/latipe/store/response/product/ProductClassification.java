package latipe.store.response.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductClassification {

    @Id
    String id = new ObjectId().toString();
    String image;
    String name;
    int quantity;
    Double price;
    Double promotionalPrice;
    String sku;
    String code;
}
