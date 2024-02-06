package latipe.product.response;

import java.util.List;
import latipe.product.viewmodel.ProductNameGetVm;

public record ProductNameListResponse(List<ProductNameGetVm> productNames) {

}
