package latipe.product.entity;

import java.util.List;
import latipe.product.entity.attribute.Attribute;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
    Boolean isDeleted = false;
    String parentCategoryId;
    String image;
    List<Attribute> attributes;
    String firstParentCategoryId;

    public Category(String name) {
        this.name = name;
    }
}
