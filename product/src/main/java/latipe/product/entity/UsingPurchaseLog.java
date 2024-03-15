package latipe.product.entity;

import java.util.ArrayList;
import java.util.List;
import latipe.product.entity.product.UsingItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "UsingPurchaseLogs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsingPurchaseLog extends AbstractAuditEntity {

  @Id
  private String id;
  private String orderId;
  private Integer status;
  private String storeId;
  private List<UsingItem> items = new ArrayList<>();
}
