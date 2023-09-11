package latipe.category.services.store.Dtos;

import jakarta.validation.constraints.NotNull;
import latipe.category.Entity.AbstractAuditEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryCreateDto extends AbstractAuditEntity {
    @NotNull(message = "Name cannot be null")
    String name;
    private String parentCategoryId;
    private String image;
}