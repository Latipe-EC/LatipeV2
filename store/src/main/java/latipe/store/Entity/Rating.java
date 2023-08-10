package latipe.store.Entity;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Ratings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rating extends AbstractAuditEntity{

    private String id;

    private String content;

    private int ratingStar;

    private String productId;

    private String userId;

    private String productName;

    private String userName;
}