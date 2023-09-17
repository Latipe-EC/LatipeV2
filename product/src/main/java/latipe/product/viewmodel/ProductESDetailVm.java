package latipe.product.viewmodel;



import latipe.product.Entity.ProductClassification;

import java.util.Date;
import java.util.List;

public record ProductESDetailVm(
        String id,
        String name,
        String slug,
        Double price,
        Boolean isPublished,
        List<String> images,
        String description,
        List<ProductClassification> productClassifications,
        List<String> classifications,
        List<String> categories,
        Boolean isBanned,
        Boolean isDeleted,
        Date createdOn
) {
}
