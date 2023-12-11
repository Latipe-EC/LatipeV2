package latipe.payment.repositories;


import java.util.List;
import java.util.Optional;
import latipe.payment.entity.Payment;
import latipe.payment.entity.enumeration.EPaymentStatus;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {

  Optional<Payment> findByOrderId(String orderId);

  @Aggregation(pipeline = {
      "{ $match: { $and: [ { $or: [ { orderId: { $regex: ?0, $options: 'i' } }, { checkoutId: { $regex: ?0, $options: 'i' } } ] }, { paymentStatus: { $in: ?3 } } ] } }",
      "{ $skip: ?1 }", "{ $limit: ?2 }"
  })
  List<Payment> findPaginate(String keyword, Long skip, Integer size,
      List<EPaymentStatus> statuses);

  @Query(value = "{ $and: [ { $or: [ { 'orderId': { $regex: ?0, $options: 'i' } }, { 'checkoutId': { $regex: ?0, $options: 'i' } } ] }, { 'paymentStatus': { $in: ?1 } } ] }", count = true)
  Long countPayment(String name, List<EPaymentStatus> statuses);
}