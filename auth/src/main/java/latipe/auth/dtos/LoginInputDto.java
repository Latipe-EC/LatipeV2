package latipe.auth.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginInputDto {
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @JsonProperty(value = "username", required = true)
    String username;
    @JsonProperty(value = "password", required = true)
    String password;
}
