package latipe.product.request;

import java.util.List;
import latipe.product.entity.attribute.Attribute;

public record UpdateAttributeCategoryRequest(List<Attribute> attributes, String categoryId) {

}
