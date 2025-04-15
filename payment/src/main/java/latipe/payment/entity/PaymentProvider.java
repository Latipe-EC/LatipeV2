package latipe.payment.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Entity representing a payment provider in the system.
 * Contains configuration and settings for different payment service providers.
 */
@Document("payment_provider")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentProvider {

    /**
     * Unique identifier for the payment provider
     */
    private String id;
    
    /**
     * Flag indicating if this payment provider is enabled
     */
    private boolean isEnabled;
    
    /**
     * Display name of the payment provider
     */
    private String name;
    
    /**
     * URL for configuring the payment provider
     */
    private String configureUrl;
    
    /**
     * Name of the UI component for displaying the payment option
     */
    private String landingViewComponentName;

    /**
     * Additional configuration settings in JSON format
     */
    @Field("additional_settings")
    private String additionalSettings;
    
    /**
     * Operation mode (e.g., "test", "live")
     */
    private String mode;
    
    /**
     * Unique code identifier for the payment provider
     */
    private String code;
}
