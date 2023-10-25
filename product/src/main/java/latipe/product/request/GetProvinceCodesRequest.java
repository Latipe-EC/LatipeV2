package latipe.product.request;

import java.util.List;
import lombok.Builder;

@Builder
public record GetProvinceCodesRequest(
    List<String> ids) {

}
