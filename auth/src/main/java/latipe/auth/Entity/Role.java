package latipe.auth.Entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collection;
import java.util.Date;

@Document(collection = "Roles")
@Data
public class Role extends AbstractAuditEntity {
    private String Id;
    private String name;
    private Boolean isDeleted= false;
    private Date createAt = new Date(new java.util.Date().getTime());
    private Date updateAt = new Date(new java.util.Date().getTime());
    public Role(String id, String name ) {
        Id = id;
        this.name = name;
    }
}
