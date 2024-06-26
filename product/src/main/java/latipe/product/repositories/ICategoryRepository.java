package latipe.product.repositories;

import java.util.List;
import latipe.product.entity.Category;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface ICategoryRepository extends MongoRepository<Category, String> {

    @Query("{ 'name' : ?0, '_id' : { $ne: ?1 } }")
    Category findByNameAndExceptId(String name, String id);

    @Query("{ name : ?0 }")
    Category findByName(String name);

    @Query("{ name: { $regex: ?0, $options: 'i'  }  }")
    List<Category> findCateByName(String name);

    @Query("{ parentCategoryId : ?0 }")
    List<Category> findChildrenCate(String id);

    @Aggregation(pipeline = {
        "{  $match: { isDeleted: false, name: { $regex: ?2, $options: 'i'  }  } }",
        "{ $sort: {image: -1, createAt: -1 } }", "{ $skip: ?0 }", "{ $limit: ?1 }"})
    List<Category> findCategoryWithPaginationAndSearch(long skip, int limit, String name);

    @Query(value = "{'name': {$regex: ?0, $options: 'i'}, 'isDeleted': false}", count = true)
    Long countByName(String name);

    @Aggregation(pipeline = {"{ 'isDeleted' : false }", "{ $skip: 0}", "{ $limit: 1 }"})
    List<Category> findFirst();

}