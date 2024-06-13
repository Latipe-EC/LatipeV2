package latipe.product.response;

import java.util.List;

public record ProductESDetailsResponse(
    String id,
    List<String> images,
    Integer countSale
) {

}
