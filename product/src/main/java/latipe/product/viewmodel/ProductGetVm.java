package latipe.product.viewmodel;


import java.util.Date;
import java.util.List;
import latipe.product.entity.Product;
import latipe.product.entity.product.ProductClassification;
import latipe.product.utils.AvgRating;


public record ProductGetVm(String id,
                           String name,
                           String slug,
                           Double price,
                           Boolean isPublished,
                           List<String> images,
                           List<ProductClassification> productClassifications,
                           Boolean isBanned,
                           Boolean isDeleted,
                           Date createdDate,
                           int countSale,
                           Double ratings) {

    public static ProductGetVm fromModel(Product product) {
        return new ProductGetVm(
            product.getId(),
            product.getName(),
            product.getSlug(),
            product.getProductClassifications().get(0).getPrice(),
            product.getIsPublished(),
            product.getImages(),
            product.getProductClassifications(),
            product.getIsBanned(),
            product.getIsDeleted(),
            product.getCreatedDate(),
            product.getCountSale(),
            AvgRating.calc(product.getRatings())
        );
    }
}
