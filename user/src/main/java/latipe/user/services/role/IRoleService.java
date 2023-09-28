package latipe.user.services.role;


import java.util.concurrent.CompletableFuture;
import latipe.user.request.CreateRoleRequest;
import latipe.user.response.RoleResponse;

public interface IRoleService {

  public CompletableFuture<RoleResponse> getOne(String id);

  public CompletableFuture<RoleResponse> create(CreateRoleRequest input);

}

