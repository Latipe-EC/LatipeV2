package latipe.search.services.store.Dtos;

import jakarta.validation.constraints.NotNull;
import latipe.search.Entity.AbstractAuditEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchCreateDto extends AbstractAuditEntity {
    @NotNull(message = "Name cannot be null")
    String name;
    private String parentSearchId;
    private String image;
}