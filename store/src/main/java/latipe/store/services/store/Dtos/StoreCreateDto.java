package latipe.store.services.store.Dtos;

import latipe.store.Entity.AbstractAuditEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StoreCreateDto extends AbstractAuditEntity {
    String name;
    String description;
    String logo;
    String ownerId;
    String cover;
}