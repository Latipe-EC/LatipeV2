package latipe.product.constants;

/**
 * Constants used throughout the Product microservice.
 * This class provides centralized access to constant values used in the product module.
 */
public class CONSTANTS {

    /**
     * Request ID header name for tracking requests through the system
     */
    public static final String REQUEST_ID = "req-id";

    /**
     * Constant for text type attributes
     */
    public final static String TEXT = "TEXT";
    
    /**
     * Constant for date type attributes
     */
    public final static String DATE = "DATE";
    
    /**
     * Constant for number type attributes
     */
    public final static String NUMBER = "NUMBER";
    
    /**
     * Minimum number of samples required for model training
     */
    public final static Integer REQUIRE_AMOUNT_TO_TRAIN = 0;
}
