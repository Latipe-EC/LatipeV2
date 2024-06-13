package latipe.product.request;

import java.util.Objects;
import latipe.product.annotations.IsObjectId;

public record OrderProductCheckRequest(
    @IsObjectId
    String productId,
    @IsObjectId
    String optionId,
    int quantity) {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OrderProductCheckRequest that = (OrderProductCheckRequest) o;
        return Objects.equals(productId, that.productId) &&
            Objects.equals(optionId, that.optionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, optionId);
    }

    public OrderProductCheckRequest merge(OrderProductCheckRequest other) {
        if (!this.equals(other)) {
            throw new IllegalArgumentException("Cannot merge different products");
        }
        return new OrderProductCheckRequest(
            productId,
            optionId,
            quantity + other.quantity
        );
    }
}
