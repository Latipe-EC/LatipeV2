package latipe.product.repositories;

import java.util.List;
import latipe.product.Entity.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface IProductRepository extends MongoRepository<Product, String> {

  @Query(value = "{ '_id' : {'$in' : ?0 } }")
  List<Product> findByIds(List<String> storeId);

}
