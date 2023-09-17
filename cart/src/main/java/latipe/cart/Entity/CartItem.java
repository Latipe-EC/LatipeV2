package latipe.cart.Entity;

import lombok.Data;

@Data
public class CartItem extends AbstractAuditEntity{
    private String id;
    private String productId;
    private String productOptionId;
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