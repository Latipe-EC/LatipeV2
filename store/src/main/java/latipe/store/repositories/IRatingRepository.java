package latipe.store.repositories;


import latipe.store.Entity.Rating;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface IRatingRepository extends MongoRepository<Rating, String> {

}
