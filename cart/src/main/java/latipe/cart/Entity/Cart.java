package latipe.cart.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "Carts")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@CompoundIndex(def = "{'userId': 1, 'productId': 1, 'productOptionId': 1}", unique = true)
public class Cart extends AbstractAuditEntity {

  @Id
  private String id;
  private String userId;
  private String productId;
  private String productOptionId = null;
  private int quantity;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Cart)) {
      return false;
    }
    return id != null && id.equals(((Cart) o).id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
