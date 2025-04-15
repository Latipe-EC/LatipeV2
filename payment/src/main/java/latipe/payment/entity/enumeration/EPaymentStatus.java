package latipe.payment.entity.enumeration;

/**
 * Enum representing the possible states of a payment transaction.
 * Used to track the status of payment processing through the system.
 */
public enum EPaymentStatus {
    /**
     * Payment is initiated but not yet completed
     */
    PENDING,
    
    /**
     * Payment has been successfully processed
     */
    COMPLETED,
    
    /**
     * Payment was cancelled before completion
     */
    CANCELLED
}
