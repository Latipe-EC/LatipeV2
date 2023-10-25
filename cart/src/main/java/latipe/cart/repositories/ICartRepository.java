package latipe.cart.repositories;

import java.util.Optional;
import latipe.cart.Entity.Cart;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface ICartRepository extends MongoRepository<Cart, String> {

  @Query("{ 'userId' : ?0 , 'isDeleted': false}")
  Page<Cart> findPaginateByUserId(String userId, Pageable pageable);

  @Query("{ 'userId' : ?0 , 'isDeleted': false}")
  Optional<Cart> findByUserId(String userId);

}
