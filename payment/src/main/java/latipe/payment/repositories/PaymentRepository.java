package latipe.payment.repositories;


import java.util.Optional;
import latipe.payment.entity.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {

  Optional<Payment> findByOrderId(String orderId);
}