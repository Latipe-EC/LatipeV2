package latipe.auth.config;

import java.util.Collection;
import java.util.List;
import latipe.auth.Entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserInfoUserDetails implements UserDetails {

  private String name;
  private String password;
  private List<GrantedAuthority> authorities;

  public UserInfoUserDetails(User userInfo) {
    name = userInfo.getEmail();
    password = userInfo.getPassword();
    authorities = (List<GrantedAuthority>) userInfo.getAuthorities();
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return name;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}