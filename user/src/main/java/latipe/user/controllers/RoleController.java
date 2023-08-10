package latipe.user.controllers;

import latipe.user.annotations.ApiPrefixController;
import latipe.user.services.role.Dtos.RoleCreateDto;
import latipe.user.services.role.Dtos.RoleDto;
import latipe.user.services.role.IRoleService;
import latipe.user.services.user.Dtos.UserCreateDto;
import latipe.user.services.user.Dtos.UserDto;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@ApiPrefixController("/role")
public class RoleController {
    private final IRoleService roleService;

    public RoleController(IRoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping(value = "create-role", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<RoleDto> createRole(@RequestBody RoleCreateDto input) {
        return roleService.create(input);
    }
}
