package latipe.payment.repositories;


import latipe.payment.Entity.PaymentProvider;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentProviderRepository extends MongoRepository<PaymentProvider, String> {
}
