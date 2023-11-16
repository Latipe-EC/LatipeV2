package latipe.product.request;

import java.util.List;
import latipe.product.annotations.IsObjectId;
import latipe.product.entity.attribute.Attribute;

public record UpdateAttributeCategoryRequest(List<Attribute> attributes,
                                             @IsObjectId
                                             String categoryId) {

}
