package latipe.rating.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Ratings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Rating extends AbstractAuditEntity {

    private String id;

    private String content;

    private int rating;

    private String productId;

    private String orderId;

    private String storeId;

    private String userId;

    private String username;
    private String avatar;

    private String orderItemId;

    private Boolean isChange = false;
}