package latipe.product.entity;

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
  private Boolean isDeleted = false;
  private String parentCategoryId;
  private String image;

  public Category(String name) {
    this.name = name;
  }
}
