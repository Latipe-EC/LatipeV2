package latipe.user.repositories;

import latipe.user.Entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface IUserRepository extends MongoRepository<User, String> {
    @Query(value = "{ $or: [ { 'email': ?0 }, { 'phone': ?0 } ] }")
    List<User> findByPhoneAndEmail(String input);

    @Query("{ $or: [ { 'email': ?0 }, { 'phone': ?0 }, { 'bio': ?0 } ] }")
    List<User> findContact(String input);

    @Query("{'tokenResetPassword' : ?0}")
    Optional<User> findByTokenResetPassword(String token);

    @Query("{'phone' : ?0}")
    Optional<User> findByPhoneNumber(String phone);
//    @Query("{'id': ?0}")
//    Optional<User> findById(String id);
}
