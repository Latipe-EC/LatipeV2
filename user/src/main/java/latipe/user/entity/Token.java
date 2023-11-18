package latipe.user.entity;

import java.time.ZonedDateTime;
import latipe.user.constants.KeyType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nonapi.io.github.classgraph.json.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Tokens")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Token extends AbstractAuditEntity {

  @Id
  private String id;
  private String userId;
  private KeyType type;
  private Boolean used = false;
  private ZonedDateTime expiredAt;

}
