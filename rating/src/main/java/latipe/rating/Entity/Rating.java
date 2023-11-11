package latipe.rating.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Ratings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rating extends AbstractAuditEntity {

  private String id;

  private String content;

  private int rating;

  private String productId;

  private String storeId;

  private String userId;

  private String detail;

  private String userName;

  private String orderId;

  private String orderItemId;
}