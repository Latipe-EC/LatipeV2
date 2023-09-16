package latipe.product.Entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "Categories")
public class Category extends AbstractAuditEntity {
    @Id
    String id;
    String name;
    private Boolean isDeleted = false;
    private String parentCategoryId;
    private String image;
}
