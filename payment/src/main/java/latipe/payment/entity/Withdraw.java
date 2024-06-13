package latipe.payment.entity;

import java.math.BigDecimal;
import latipe.payment.entity.enumeration.EWithdrawStatus;
import latipe.payment.entity.enumeration.EWithdrawType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("withdraw")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Withdraw extends AbstractAuditEntity {

    @Id
    private String id;
    private String userId;
    private EWithdrawStatus withdrawStatus;
    private BigDecimal amount;
    private String gatewayTransactionId;
    private String failureMessage;
    private EWithdrawType type;
    private String emailRecipient;
    private String checkoutId;
    private String orderId;


}
