package latipe.auth.entity;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Roles")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Role extends AbstractAuditEntity {

  private String id;
  private String name;
  private Boolean isDeleted = false;
  private Date createAt = new Date(new java.util.Date().getTime());
  private Date updateAt = new Date(new java.util.Date().getTime());

  public Role(String id, String name) {
    this.id = id;
    this.name = name;
  }

  public Role(String name) {
    this.name = name;
  }
}
