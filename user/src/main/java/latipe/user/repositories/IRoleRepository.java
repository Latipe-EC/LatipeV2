package latipe.user.repositories;

import latipe.user.Entity.Role;
import latipe.user.Entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import org.springframework.data.mongodb.repository.Query;

public interface IRoleRepository extends MongoRepository<Role, String> {
    @Query("{'name': ?0}")
    Optional<Role> findRoleByName(String name);

    Boolean existsByName(String name);
}
