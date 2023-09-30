package latipe.user.repositories;

import java.util.Optional;
import latipe.user.entity.Role;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface IRoleRepository extends MongoRepository<Role, String> {

  @Query("{'name': ?0}")
  Optional<Role> findRoleByName(String name);

  Boolean existsByName(String name);
}
