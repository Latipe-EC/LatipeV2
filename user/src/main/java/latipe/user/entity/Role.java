package latipe.user.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Roles")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Role extends AbstractAuditEntity {

    @Id
    private String id;
    private String name;
    private Boolean isDeleted = false;

    public Role(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Role(String name) {
        this.name = name;
    }
}
