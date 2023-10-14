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
@Document(collection = "AttributeCategories")
public class AttributeCategory extends AbstractAuditEntity {

  @Id
  String id;
  List<Attribute> attributes;
  String categoryId;

}
