package latipe.product.viewmodel;

import latipe.product.request.UpdateProductQuantityRequest;

import java.util.List;

public record RollbackProductMessage(
        String orderId,
        Integer status) {

}
