package latipe.product.entity;

import java.util.ArrayList;
import java.util.List;
import latipe.product.entity.attribute.AttributeValue;
import latipe.product.entity.product.ProductClassification;
import latipe.product.entity.product.ProductVariant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product extends AbstractAuditEntity {

    @Id
    private String id;
    private Boolean isBanned = false;
    private Boolean isDeleted = false;
    private Boolean isPublished = true;
    private List<AttributeValue> detailsProduct;
    private List<Integer> ratings = List.of(0, 0, 0, 0, 0);
    private String name;
    private String description;
    private List<String> categories;
    private String slug;
    private int countSale = 0;
    private String storeId;
    private List<String> images = new ArrayList<>();
    private List<ProductClassification> productClassifications = new ArrayList<>();
    private List<ProductVariant> productVariants = new ArrayList<>();
    private String reasonBan;
    private List<Integer> indexFeatures = new ArrayList<>();

    public Product(String prodId) {
        this.id = prodId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Product product = (Product) o;
        return id.equals(product.getId());
    }

}
