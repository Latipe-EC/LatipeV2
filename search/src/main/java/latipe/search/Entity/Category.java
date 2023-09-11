package latipe.search.Entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "Categories")
public class Search {
    @Id
    String id;
    String name;
    private Boolean isDeleted = false;
    private String parentSearchId;
    private String image;
}
