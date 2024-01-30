package latipe.product.viewmodel;

import java.util.List;
import latipe.product.request.UpdateProductQuantityRequest;

public record UpdateProductQuantityVm(
    String orderId,
    String storeId,
    List<UpdateProductQuantityRequest> items
) {

}
