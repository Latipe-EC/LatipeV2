package latipe.product.response;

import java.util.List;
import latipe.product.viewmodel.ProductOrderVm;
import lombok.Builder;

@Builder
public record OrderProductResponse(
    List<ProductOrderVm> products,
    List<String> storeProvinceCodes,
    Double totalPrice) {

}
