package latipe.user.services.role;


import latipe.user.services.IService;
import latipe.user.services.role.Dtos.RoleCreateDto;
import latipe.user.services.role.Dtos.RoleDto;
import latipe.user.services.role.Dtos.RoleUpdateDto;

public interface IRoleService extends IService<RoleDto, RoleCreateDto, RoleUpdateDto> {
//    @Query("{'id' : ?0}")
//    Role findById(String id);
}

