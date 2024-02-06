package latipe.product.repositories;

import java.util.List;
import latipe.product.entity.Product;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface IProductRepository extends MongoRepository<Product, String> {

  @Query(value = "{'storeId': ?0, isBanned: false, isDeleted: false, 'name': { $regex: ?1, $options: 'i' }}", count = true)
  Long countProductByStoreId(String id, String name);

  @Query(value = "{isBanned:  { $in: ?0 },  'name': { $regex: ?1, $options: 'i' }}", count = true)
  Long countAdminProduct(List<Boolean> isBanned, String keyword);

  @Query(value = "{'storeId': ?0, isBanned: true, isDeleted: false,  'name': { $regex: ?1, $options: 'i' }}", count = true)
  Long countProductBanByStoreId(String id, String name);

  @Query(value = "{_id:  { $in: ?0 },  storeId: ?1 }")
  List<Product> findAllByIdsAndStoreId(List<String> ids, String storeId);

  @Aggregation(pipeline = {
      "{ $match: { $or: [ { 'categoryId': ?2 }, { 'categoryId': { $exists: false } } ], 'name': { $regex: ?1, $options: 'i' } } }",
      "{ $skip: ?3 }",
      "{ $limit: ?4 }"
  })
  List<Product> searchProductTemp(String keyword, String category, long skip,
      Integer limit);


}
