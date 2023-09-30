package latipe.user.mappers;

import latipe.user.entity.Role;
import latipe.user.request.CreateRoleRequest;
import latipe.user.response.RoleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)

public interface IRoleMapper {

  @Mappings({
      @Mapping(target = "id", ignore = true),
  })
  public abstract Role mapBeforeCreate(CreateRoleRequest role);

  public abstract RoleResponse mapToResponse(Role role);
}
