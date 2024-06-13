package latipe.rating.repositories;


import latipe.rating.entity.Rating;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface IRatingRepository extends MongoRepository<Rating, String> {

    @Query(value = "{'storeId': ?0}", count = true)
    Long countRatingByStoreId(String id);

    @Query(value = "{'productId': ?0}", count = true)
    Long countRatingByProductId(String id);
}
