package latipe.payment.entity;

import java.math.BigDecimal;
import latipe.payment.entity.enumeration.EPaymentMethod;
import latipe.payment.entity.enumeration.EPaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Entity representing a payment transaction in the system.
 * Stores details about payments, including amount, status, and related order information.
 */
@Document("payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment extends AbstractAuditEntity {

    /**
     * Unique identifier for the payment
     */
    @Id
    private String id;
    
    /**
     * ID of the order associated with this payment
     */
    private String orderId;
    
    /**
     * ID of the user who made the payment
     */
    private String userId;
    
    /**
     * ID of the checkout session
     */
    private String checkoutId;
    
    /**
     * The payment amount
     */
    private BigDecimal amount;
    
    /**
     * Service fee charged for processing the payment
     */
    private Double paymentFee;
    
    /**
     * Email address of the user who made the payment
     */
    private String email;
    
    /**
     * Method used for the payment (e.g., COD, BANKING, PAYPAL)
     */
    private EPaymentMethod paymentMethod;
    
    /**
     * Current status of the payment
     */
    private EPaymentStatus paymentStatus;
    
    /**
     * Transaction ID from the payment gateway
     */
    private String gatewayTransactionId;
    
    /**
     * Error message if payment failed
     */
    private String failureMessage;
    
    /**
     * Indicates whether this payment has been refunded
     */
    private Boolean isRefund = false;
}
