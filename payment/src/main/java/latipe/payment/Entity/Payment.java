package latipe.payment.Entity;

import java.math.BigDecimal;
import latipe.payment.Entity.enumeration.EPaymentMethod;
import latipe.payment.Entity.enumeration.EPaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends AbstractAuditEntity {

  @Id
  private String id;
  private String orderId;
  private String userId;
  private String checkoutId;
  private BigDecimal amount;
  private Double paymentFee;
  private EPaymentMethod paymentMethod;
  private EPaymentStatus paymentStatus;
  private String gatewayTransactionId;
  private String failureMessage;
}
