package latipe.user.services.role;

import java.util.concurrent.CompletableFuture;
import latipe.user.Entity.Role;
import latipe.user.exceptions.NotFoundException;
import latipe.user.mappers.IRoleMapper;
import latipe.user.repositories.IRoleRepository;
import latipe.user.request.CreateRoleRequest;
import latipe.user.response.RoleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleService implements IRoleService {
    private final IRoleRepository roleRepository;
    private final IRoleMapper roleMapper;


    @Async
    @Override
    public CompletableFuture<RoleResponse> getOne(String id) {
        return CompletableFuture.supplyAsync(() -> {
            Role role = roleRepository.findById(id).orElseThrow(() -> new NotFoundException("Cannot find role with id: {}" + id));
            return roleMapper.mapToResponse(role);
        });
    }

    @Async
    @Override
    public CompletableFuture<RoleResponse> create(CreateRoleRequest input) {
        return CompletableFuture.supplyAsync(() -> {
            var  role = roleMapper.mapBeforeCreate(input);
            var res = roleRepository.save(role);
            return roleMapper.mapToResponse(res);
        });
    }
}
