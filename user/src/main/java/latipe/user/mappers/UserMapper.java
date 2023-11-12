package latipe.user.mappers;

import latipe.user.entity.User;
import latipe.user.entity.UserAddress;
import latipe.user.request.CreateUserAddressRequest;
import latipe.user.request.CreateUserRequest;
import latipe.user.request.RegisterRequest;
import latipe.user.request.UpdateUserAddressRequest;
import latipe.user.request.UpdateUserRequest;
import latipe.user.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class UserMapper {

  public abstract void mapBeforeUpdateUserAddress(@MappingTarget UserAddress address,
      UpdateUserAddressRequest input);

  public abstract void mapBeforeUpdateUser(@MappingTarget User user, UpdateUserRequest input);

  @Mappings({@Mapping(target = "role", ignore = true)})
  public abstract User mapBeforeCreateUserAddress(CreateUserRequest input, String displayName,
      String password);

  @Mappings({@Mapping(target = "roleId", source = "roleId"),
      @Mapping(target = "displayName", source = "displayName"),
      @Mapping(target = "hashedPassword", source = "password"),})
  public abstract User mapBeforeCreate(RegisterRequest input, String roleId, String displayName,
      String password);

  @Mappings({@Mapping(target = "role", source = "user.role.name")})
  public abstract UserResponse mapToResponse(User user);

  public abstract UserAddress mapToUserAddress(String id, CreateUserAddressRequest address);
}
