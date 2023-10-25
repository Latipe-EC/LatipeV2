package latipe.user.mappers;

import latipe.user.entity.User;
import latipe.user.entity.UserAddress;
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
public interface IUserMapper {

  void mapBeforeUpdateUserAddress(@MappingTarget UserAddress address,
      UpdateUserAddressRequest input);

  void mapBeforeUpdateUser(@MappingTarget User user, UpdateUserRequest input);

  @Mappings({@Mapping(target = "role", ignore = true)})
  User mapBeforeCreateUserAddress(CreateUserRequest input, String displayName, String password);

  @Mappings({@Mapping(target = "roleId", source = "roleId"),
      @Mapping(target = "displayName", source = "displayName"),
      @Mapping(target = "hashedPassword", source = "password"),})
  User mapBeforeCreate(RegisterRequest input, String roleId, String displayName, String password);

  @Mappings({@Mapping(target = "role", source = "user.role.name")})
  UserResponse mapToResponse(User user);
}
