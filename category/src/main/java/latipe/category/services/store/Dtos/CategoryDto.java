package latipe.category.services.store.Dtos;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CategoryDto extends CategoryUpdateDto {
    @JsonProperty(value = "id", required = true)
    public String id;
    @JsonProperty(value = "isDeleted")
    public Boolean isDeleted = false;
    boolean isBanned = false;
    boolean isPublished = true;
}

