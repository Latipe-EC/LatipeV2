package latipe.user.services.role;

import latipe.user.Entity.Role;
import latipe.user.exceptions.NotFoundException;
import latipe.user.repositories.IRoleRepository;
import latipe.user.services.role.Dtos.RoleCreateDto;
import latipe.user.services.role.Dtos.RoleDto;
import latipe.user.services.role.Dtos.RoleUpdateDto;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class RoleService implements IRoleService {
    private final ModelMapper toDto;
    private final IRoleRepository roleRepository;

    public RoleService(ModelMapper toDto, IRoleRepository roleRepository) {
        this.toDto = toDto;
        this.roleRepository = roleRepository;
    }

    @Override
    public CompletableFuture<List<RoleDto>> getAll() {
        return null;
    }

    @Async
    @Override
    public CompletableFuture<RoleDto> getOne(String id) {
        return CompletableFuture.supplyAsync(() -> {
            Role role = roleRepository.findById(id).orElseThrow(() -> new NotFoundException("Cannot find role with id: {}" + id));
            toDto.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
            return toDto.map(role, RoleDto.class);
        });
    }

    @Async
    @Override
    public CompletableFuture<RoleDto> create(RoleCreateDto input) {
        return CompletableFuture.supplyAsync(() -> {
            Role role = toDto.map(input, Role.class);
            toDto.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
            return toDto.map(roleRepository.save(role), RoleDto.class);
        });
    }

    @Override
    public CompletableFuture<RoleDto> update(String id, RoleUpdateDto input) {
        return null;
    }

    @Override
    public CompletableFuture<Void> remove(String id) {
        return null;
    }

}
