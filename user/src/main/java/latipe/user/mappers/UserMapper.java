package latipe.user.mappers;

import java.time.ZonedDateTime;
import latipe.user.constants.KeyType;
import latipe.user.entity.Token;
import latipe.user.entity.User;
import latipe.user.entity.UserAddress;
import latipe.user.request.CreateUserAddressRequest;
import latipe.user.request.CreateUserRequest;
import latipe.user.request.RegisterRequest;
import latipe.user.request.UpdateUserAddressRequest;
import latipe.user.request.UpdateUserRequest;
import latipe.user.response.UserResponse;
import latipe.user.viewmodel.RegisterMessage;
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

  @Mappings({@Mapping(target = "role", ignore = true),
      @Mapping(target = "roleId", source = "roleId"),
      @Mapping(target = "hashedPassword", source = "password"),
      @Mapping(target = "displayName", source = "displayName"),})
  public abstract User mapBeforeCreate(CreateUserRequest input, String displayName, String password,
      String roleId, String username);

  @Mappings({@Mapping(target = "roleId", source = "roleId"),
      @Mapping(target = "displayName", source = "displayName"),
      @Mapping(target = "hashedPassword", source = "password"),})
  public abstract User mapBeforeCreate(RegisterRequest input, String roleId, String displayName,
      String password, String username);

  @Mappings({@Mapping(target = "role", source = "user.role.name"),
      @Mapping(target = "username", expression = "java(user.getUsernameReal())")}
  )
  public abstract UserResponse mapToResponse(User user);

  public abstract UserAddress mapToUserAddress(String id, CreateUserAddressRequest address);

  public abstract RegisterMessage mapToMessage(String userId, String type, String name,
      String email, String password, String token);


  public abstract Token mapToToken(String userId, KeyType type, ZonedDateTime expiredAt);
}
