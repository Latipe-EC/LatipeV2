package latipe.product.response;

import java.util.List;
import java.util.Map;
import latipe.product.viewmodel.ProductGetVm;

public record ProductListGetResponse(
    List<ProductGetVm> products,
    int pageNo,
    int pageSize,
    long totalElements,
    int totalPages,
    boolean isLast
) {

}
