package latipe.user.services.role.Dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class RoleCreateDto {
    @NotEmpty(message = "Role Name  is required")
    private String name;
}