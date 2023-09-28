package latipe.auth.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import latipe.auth.Entity.User;
import latipe.auth.exceptions.ForbiddenException;
import latipe.auth.exceptions.NotFoundException;
import latipe.auth.repositories.IUserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

  private final IUserRepository userRepository;

  public UserService(IUserRepository userRepository) {
    this.userRepository = userRepository;
  }


  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    final List<User> users = userRepository.findByPhoneAndEmail(username);
    if (users.isEmpty()) {
      throw new NotFoundException("Cannot find user with email or phone");
    }
    if (users.get(0).getPoint() < -100) {
      throw new ForbiddenException("Your account has been locked");
    } else if (users.get(0).getIsDeleted()) {
      throw new NotFoundException("Tài khoản của bạn đã bị xóa");
    }
    Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
    authorities.add(new SimpleGrantedAuthority(users.get(0).getAuthorities().toString()));
    return new org.springframework.security.core.userdetails.User(users.get(0).getUsername(),
        users.get(0).getPassword(), authorities);
  }
}
