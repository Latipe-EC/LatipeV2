package latipe.product.response;

import java.util.List;

public record ProductSIEResponse(
    String product_id,
    String product_name,
    List<String> image_urls
) {

}
