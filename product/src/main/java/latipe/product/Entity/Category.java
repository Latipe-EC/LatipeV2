package latipe.product.Entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "Categories")
public class Category {
    @Id
    String id;
    String name;
    private Boolean isDeleted = false;
    private String parentCategoryId;
    private String image;
}
