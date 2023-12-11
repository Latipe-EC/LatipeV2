package latipe.user.utils;

import jakarta.annotation.PostConstruct;
import latipe.user.constants.CONSTANTS;
import latipe.user.entity.Role;
import latipe.user.repositories.IRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class CommandHandler {

  private final IRoleRepository roleRepository;

  @PostConstruct
  public void init() {
//    var users = userRepository.findAll();
//    for (var user : users) {
//      user.setIsBanned(false);
//    }
//    userRepository.saveAll(users);

    // Check if role exists and create role if it does not exist

    if (!roleRepository.existsByName(CONSTANTS.USER)) {
      Role role = new Role(CONSTANTS.USER);
      roleRepository.save(role);
    }
    if (!roleRepository.existsByName(CONSTANTS.VENDOR)) {
      Role role = new Role(CONSTANTS.VENDOR);
      roleRepository.save(role);
    }
    if (!roleRepository.existsByName(CONSTANTS.DELIVERY)) {
      Role role = new Role(CONSTANTS.DELIVERY);
      roleRepository.save(role);
    }
    if (!roleRepository.existsByName(CONSTANTS.ADMIN)) {
      Role role = new Role(CONSTANTS.ADMIN);
      roleRepository.save(role);
    }
  }

}