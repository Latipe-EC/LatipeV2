package latipe.user.Entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "Roles")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Role extends AbstractAuditEntity {
    @Id
    private String id;
    private String name;
    private Boolean isDeleted= false;
    public Role(String id, String name ) {
        this.id = id;
        this.name = name;
    }
    public Role(String name ) {
        this.name = name;
    }
}
