package latipe.product.repositories;


import java.util.Optional;
import latipe.product.entity.UsingPurchaseLog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface IUsingPurchaseLogsRepository extends MongoRepository<UsingPurchaseLog, String> {

  Optional<UsingPurchaseLog> findUsingPurchaseLogByOrderId(String orderId);
}
