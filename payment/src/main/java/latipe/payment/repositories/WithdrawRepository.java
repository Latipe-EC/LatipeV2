package latipe.payment.repositories;


import java.util.Optional;
import latipe.payment.entity.Withdraw;
import latipe.payment.entity.enumeration.EWithdrawStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WithdrawRepository extends MongoRepository<Withdraw, String> {

  Optional<Withdraw> findByUserIdAndWithdrawStatus(String userId, EWithdrawStatus withdrawStatus);
  Optional<Withdraw> findByOrderId(String orderId);
}