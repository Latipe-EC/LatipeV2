package latipe.product.repositories;


import latipe.product.entity.UsingPurchaseLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface IUsingPurchaseLogsRepository extends MongoRepository<UsingPurchaseLog, String> {

  Optional<UsingPurchaseLog> findUsingPurchaseLogByOrderId(String orderId);
}
