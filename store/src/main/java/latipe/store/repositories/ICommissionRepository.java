package latipe.store.repositories;


import latipe.store.Entity.Commission;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ICommissionRepository extends MongoRepository<Commission, String> {

  Boolean existsByFeeOrder(Double feeOrder);
}
