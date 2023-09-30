package latipe.user.mappers;

import latipe.user.Entity.Role;
import latipe.user.Entity.User;
import latipe.user.Entity.UserAddress;
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

  @Mappings({
      @Mapping(target = "id", ignore = true),
  })
  public abstract void mapBeforeUpdateUserAddress(@MappingTarget UserAddress userAddress,
      UpdateUserAddressRequest user);

  public abstract void mapBeforeUpdateUser(@MappingTarget User user,
      UpdateUserRequest input);

  @Mappings({@Mapping(target = "role", ignore = true)})
  public abstract User mapBeforeCreateUserAddress(CreateUserRequest input, String displayName,
      String password);

  @Mappings({
      @Mapping(target = "role", source = "role"),
      @Mapping(target = "displayName", source = "displayName"),
      @Mapping(target = "hashedPassword", source = "password"),
  })
  public abstract User mapBeforeCreate(RegisterRequest input, Role role, String displayName,
      String password);

  @Mappings({@Mapping(target = "role", source = "user.role.name")})
  public abstract UserResponse mapToResponse(User user);
}
