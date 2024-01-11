package latipe.auth.entity;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Document(collection = "Users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User extends AbstractAuditEntity implements UserDetails {

  @Id
  private String id;
  private String firstName;
  private String lastName;
  private String displayName;
  private int requestCount = 0;
  private Date lastRequest;
  private String email;
  private boolean isRequiredVerify = false;
  private Date verifiedAt;
  private Date lastLogin;
  private String hashedPassword;
  private String avatar;
  private String bio;
  private int point = 0;
  private double eWallet;
  private String storeId;
  private String tokenResetPassword;
  private Date createAt = new Date();
  private Date updateAt = new Date();
  private Boolean isDeleted = false;
  private Role role;
  @Field(targetType = FieldType.OBJECT_ID)
  private String roleId;
  private String phoneNumber;
  private String username;
  private Boolean isChangeUsername = false;
  private Gender gender;
  private ZonedDateTime birthday;
  private Boolean isBanned = false;
  private String reasonBan;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority(role.getName()));
  }

  @Override
  public String getPassword() {
    return this.hashedPassword;
  }

  @Override
  public String getUsername() {
    return this.email;
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
    return !this.isDeleted;
  }
}
