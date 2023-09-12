package latipe.product.Entity;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product extends AbstractAuditEntity {
    @Id
    private String id;
    private String name;
    private String description;
    private List<String> categories;
    private String slug;
    @Min(0)
    private Double price;
    @Min(0)
    private int quantity;
    private String storeId;
    private List<String> images = new ArrayList<>();
    private List<ProductClassification> productClassifications = new ArrayList<>();
    private List<ProductVariant> productVariant = new ArrayList<>();
    private String reasonBan;
    boolean isBanned = false;
    boolean isDeleted = false;
    boolean isPublished = true;
}
