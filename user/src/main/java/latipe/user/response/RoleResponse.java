package latipe.user.response;

import latipe.user.Entity.Role;
import latipe.user.Entity.UserAddress;
import latipe.user.request.CreateRoleRequest;
import latipe.user.request.UpdateUserAddressRequest;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

public record RoleResponse(String id, String name) {

}
