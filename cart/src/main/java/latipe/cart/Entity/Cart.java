package latipe.cart.Entity;

import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "Carts")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Cart extends AbstractAuditEntity {

  @Id
  private String id;
  private String userId;
  private Set<CartItem> cartItems = new HashSet<>();
  private Boolean isDeleted = false;

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
    // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
    return getClass().hashCode();
  }
}
