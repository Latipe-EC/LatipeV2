package latipe.product.request;

import java.util.List;

public record ProductESDetailsRequest(
    List<String> product_ids) {

}
