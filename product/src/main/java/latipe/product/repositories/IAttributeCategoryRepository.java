package latipe.product.repositories;

import java.util.List;
import java.util.Optional;
import latipe.product.entity.AttributeCategory;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface IAttributeCategoryRepository extends MongoRepository<AttributeCategory, String> {

    Optional<AttributeCategory> findByCategoryId(String categoryId);

    @Aggregation(pipeline = {
        "{$skip: ?0}",
        "{$limit: ?1}"
    })
    List<AttributeCategory> findAll(long skip, int limit);
}
