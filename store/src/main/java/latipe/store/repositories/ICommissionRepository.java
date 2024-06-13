package latipe.store.repositories;


import java.util.List;
import latipe.store.entity.Commission;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface ICommissionRepository extends MongoRepository<Commission, String> {

    Boolean existsByFeeOrder(Double feeOrder);

    @Aggregation(pipeline = {
        "{  $match: {  name: { $regex: ?0, $options: 'i'  }  } }",
        "{ $skip: ?1 }", "{ $limit: ?2 }"})
    List<Commission> findPaginate(String keyword, Long skip, Integer size);

    @Query(value = "{'name': {$regex: ?0, $options: 'i'}}", count = true)
    Long countCommission(String name);
}
