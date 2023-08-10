
package latipe.user.services.role.Dtos;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.UUID;

@Data
public class RoleDto extends RoleUpdateDto {
    @JsonProperty(value = "id", required = true)
    public String id;
    @JsonProperty(value = "isDeleted")
    public Boolean isDeleted  = false;
    private Date createdDate;
    private Date lastModifiedDate;
}

