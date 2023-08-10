package latipe.user.Entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "Roles")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Role extends AbstractAuditEntity {
    @Id
    private String id;
    private String name;
    private Boolean isDeleted= false;
    public Role(String id, String name ) {
        id = id;
        this.name = name;
    }
}
