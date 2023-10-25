package latipe.cart.Entity;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class CartItem extends AbstractAuditEntity {
  @Id
  private String id;
  private String productId;
  private String productOptionId = null;
  private int quantity;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CartItem)) {
      return false;
    }
    return id != null && id.equals(((CartItem) o).id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
