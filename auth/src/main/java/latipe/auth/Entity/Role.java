package latipe.auth.Entity;

import lombok.Data;

import java.util.Collection;
import java.util.Date;

@Data
public class Role {
    private String Id;
    private String name;
    private Boolean isDeleted= false;
    private Date createAt = new Date(new java.util.Date().getTime());
    private Date updateAt = new Date(new java.util.Date().getTime());
    private Collection<User> usersByRoleId;
    public Role(String id, String name ) {
        Id = id;
        this.name = name;
    }
    public Role() {

    }
}
