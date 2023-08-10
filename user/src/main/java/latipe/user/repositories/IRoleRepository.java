package latipe.user.repositories;

import latipe.user.Entity.Role;
import latipe.user.Entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface IRoleRepository extends MongoRepository<Role, String> {
    Optional<Role> findRoleByName(String name);
}
