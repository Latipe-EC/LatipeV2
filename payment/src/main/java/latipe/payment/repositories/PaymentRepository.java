package latipe.payment.repositories;


import latipe.payment.Entity.Payment;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;
@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {
}