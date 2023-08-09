package latipe.auth.dtos;


import com.fasterxml.jackson.annotation.JsonProperty;
import latipe.auth.Entity.Role;
import lombok.Data;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class UserProfileDto {
    @JsonProperty(value = "id")
    private String id;
    @JsonProperty(value = "firstName")
    private String firstName;
    @JsonProperty(value = "lastName")
    private String lastName;
    @JsonProperty(value = "displayName")
    private String displayName;
    @JsonProperty(value = "phone")
    private String phone;
    @JsonProperty(value = "email")
    private String email;
    @JsonProperty(value = "bio")
    private String bio;
    private Role role;
    @JsonProperty(value = "lastActiveAt")
    private Date lastActiveAt;
}
