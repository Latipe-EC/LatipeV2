package latipe.product.entity;

import jakarta.validation.constraints.Min;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product extends AbstractAuditEntity {

  boolean isBanned = false;
  boolean isDeleted = false;
  boolean isPublished = true;
  Double promotionalPrice;
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
  private int countSale = 0;
  private String storeId;
  private List<String> images = new ArrayList<>();
  private List<ProductClassification> productClassifications = new ArrayList<>();
  private List<ProductVariant> productVariants = new ArrayList<>();
  private String reasonBan;

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
