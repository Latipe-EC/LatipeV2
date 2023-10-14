package latipe.product.response;

import java.util.List;
import latipe.product.entity.attribute.Attribute;

public record AttributeCategoryResponse(
    String id,
    List<Attribute> attributes, String categoryId) {

}
