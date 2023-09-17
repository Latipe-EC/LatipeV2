package latipe.cart.repositories;

import latipe.cart.Entity.Cart;
import latipe.cart.Entity.CartItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ICartRepository extends MongoRepository<Cart, String> {
    @Query("{ 'userId' : ?0 , 'isDeleted': false}")
    Page<Cart> findPaginateByUserId(String userId, Pageable pageable);
    @Query("{ 'userId' : ?0 , 'isDeleted': false}")
    Optional<Cart> findByUserId(String userId);
}
