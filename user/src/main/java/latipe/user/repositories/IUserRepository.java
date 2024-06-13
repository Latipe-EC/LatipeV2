package latipe.user.repositories;

import java.util.List;
import java.util.Optional;
import latipe.user.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface IUserRepository extends MongoRepository<User, String> {

    @Query(value = "{ $or: [ { 'email': ?0 }, { 'phoneNumber': ?0 } ] }")
    List<User> findByPhoneAndEmail(String input);

    @Query(value = "{  'email': ?0  }")
    Optional<User> findByEmail(String email);

    Boolean existsByUsername(String username);

    @Query(value = "{ 'isBanned': { $in: ?0 }, $or: [ { 'email': { $regex: ?1, $options: 'i' } }, { 'phoneNumber': { $regex: ?1, $options: 'i' } }, { 'username': { $regex: ?1, $options: 'i' } } ] }", count = true)
    long countUserAdmin(List<Boolean> isBanned, String keyword);

//    @Query("{'id': ?0}")
//    Optional<User> findById(String id);
}
