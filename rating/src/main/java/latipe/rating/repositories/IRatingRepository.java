package latipe.rating.repositories;


import java.util.List;
import latipe.rating.Entity.Rating;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface IRatingRepository extends MongoRepository<Rating, String> {

  List<Rating> findByStoreId(String storeId);
  List<Rating> findByProductId(String productId);
}
