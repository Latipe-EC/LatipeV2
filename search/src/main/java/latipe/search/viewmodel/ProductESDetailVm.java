package latipe.search.viewmodel;


import java.util.Date;
import java.util.List;
import latipe.search.document.ProductClassification;

public record ProductESDetailVm(
    String id,
    String name,
    String slug,
    Double price,
    Boolean isPublished,
    int countSale,
    List<String> images,
    String description,
    List<ProductClassification> productClassifications,
    List<String> classifications,
    List<String> categories,
    Boolean isBanned,
    Boolean isDeleted,
    Date createdDate,
    Double ratings
) {

}
