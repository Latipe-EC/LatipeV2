package latipe.product.response;


import java.util.Date;
import java.util.List;
import latipe.product.entity.attribute.AttributeValue;
import latipe.product.entity.product.ProductClassification;
import latipe.product.entity.product.ProductVariant;

public record ProductDetailResponse(String id, String name, String slug, Double price,
                                    Double promotionalPrice, Boolean isPublished,
                                    List<String> images, String description,
                                    List<ProductClassification> productClassifications,
                                    List<ProductVariant> productVariants,
                                    List<CategoryResponse> categories,
                                    List<AttributeValue> detailsProduct, Boolean isBanned,
                                    Boolean isDeleted, Date createdOn, StoreResponse store,
                                    List<Integer> ratings) {

}
