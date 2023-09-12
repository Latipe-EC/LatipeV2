package latipe.search.viewmodel;


import latipe.search.document.Product;
import latipe.search.document.ProductClassification;

import java.util.Date;
import java.util.List;

public record ProductGetVm(String id,
                           String name,
                           String slug,
                           Double price,
                           Boolean isPublished,
                           List<String> images,
                           List<ProductClassification> productClassifications,
                           Boolean isBanned,
                           Boolean isDeleted,
                           Date createdOn) {
    public static ProductGetVm fromModel(Product product) {
        return new ProductGetVm(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getPrice(),
                product.getIsPublished(),
                product.getImages(),
                product.getProductClassifications(),
                product.isBanned(),
                product.isDeleted(),
                product.getCreatedDate()
        );
    }
}
