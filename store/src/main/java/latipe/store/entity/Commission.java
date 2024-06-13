package latipe.store.entity;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "Commissions")
public class Commission {

    @Id
    String id;
    String name;
    Double feeOrder;
    Integer minPoint;
}
