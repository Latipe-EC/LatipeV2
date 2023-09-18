package latipe.payment.Entity;

import latipe.payment.Entity.enumeration.EPaymentMethod;
import latipe.payment.Entity.enumeration.EPaymentStatus;
import lombok.*;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Document("payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends AbstractAuditEntity {
    @Id
    private Long id;
    private Long orderId;
    private String checkoutId;
    private BigDecimal amount;
    private Double paymentFee;
    private EPaymentMethod paymentMethod;
    private EPaymentStatus paymentStatus;
    private String gatewayTransactionId;
    private String failureMessage;
}
