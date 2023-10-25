package latipe.product.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import latipe.product.entity.attribute.Attribute;

public record CategoryResponse(@JsonProperty(value = "id", required = true) String id,
                               @NotNull(message = "Name cannot be null") String name,
                               String parentCategoryId, String image, List<Attribute> attributes,
                               @JsonProperty(value = "isDeleted") Boolean isDeleted) {

}

