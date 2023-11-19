package latipe.cart.repositories;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import latipe.cart.Entity.Cart;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface ICartRepository extends MongoRepository<Cart, String> {

  @Aggregation(pipeline = {"{ $match: { userId: ?0} }", "{ $skip: ?1 }", "{ $limit: ?2 }"})
  List<Cart> findMyCart(String userId, Long skip, int limit);

  Long countByUserId(String userId);

  @Query("{ 'userId' : ?0 , 'productOptionId' : ?1 , 'productId' : ?2}")
  Optional<Cart> findByUserIdAndProductOptionIdAndProductId(String userId, String productOptionId,
      String productId);

  @Query("{'id' : { $in: ?0 }, 'userId' : ?1}")
  List<Cart> findAllByIdAndUserId(Set<String> ids, String userId);

}
