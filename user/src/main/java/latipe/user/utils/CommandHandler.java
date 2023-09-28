package latipe.user.utils;

import jakarta.annotation.PostConstruct;
import latipe.user.Entity.Role;
import latipe.user.constants.CONSTANTS;
import latipe.user.repositories.IRoleRepository;
import org.springframework.stereotype.Component;


@Component

public class CommandHandler {

  private final IRoleRepository roleRepository;

  public CommandHandler(IRoleRepository roleRepository) {
    this.roleRepository = roleRepository;
  }

  @PostConstruct
  public void init() {
    // Check if role exists and create role if it does not exist
    if (!roleRepository.existsByName(CONSTANTS.USER)) {
      Role role = new Role(CONSTANTS.USER);
      roleRepository.save(role);
    }
    if (!roleRepository.existsByName(CONSTANTS.VENDOR)) {
      Role role = new Role(CONSTANTS.VENDOR);
      roleRepository.save(role);
    }
    if (!roleRepository.existsByName(CONSTANTS.ADMIN)) {
      Role role = new Role(CONSTANTS.ADMIN);
      roleRepository.save(role);
    }
  }

  public void handleCommand() {
    // Handle command here
  }
}