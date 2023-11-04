package latipe.product.repositories;

import java.util.List;
import latipe.product.entity.Product;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface IProductRepository extends MongoRepository<Product, String> {

  @Query(value = "{ '_id' : {'$in' : ?0 } }")
  List<Product> findByIds(List<String> storeId);

  @Aggregation(pipeline = {"{  $match: { name: { $regex: ?0, $options: 'i'  }  } }",
      "{ $sort: { createAt: -1 } }", "{ $skip: ?1 }", "{ $limit: ?2 }"})
  List<Product> getMyProductStore(long skip, int limit, String name, String orderBy);

  @Query(value = "{'storeId': ?0, isBanned: false, isDeleted: false, 'name': { $regex: ?1, $options: 'i' }}", count = true)
  Long countProductByStoreId(String id, String name);

  @Query(value = "{'storeId': ?0, isBanned: true, isDeleted: false,  'name': { $regex: ?1, $options: 'i' }}", count = true)
  Long countProductBanByStoreId(String id, String name);
}
