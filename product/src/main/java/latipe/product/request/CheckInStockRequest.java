package latipe.product.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CheckInStockRequest(
        @NotNull(message = "cannot be null")
        @JsonProperty("products")
        List<OrderProductCheckRequest> prodOrders
) {
}