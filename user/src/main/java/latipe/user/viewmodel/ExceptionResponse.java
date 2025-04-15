package latipe.user.viewmodel;

import java.util.ArrayList;
import java.util.List;

/**
 * Record representing a standardized API exception response.
 * This class provides a consistent format for error responses across the user service.
 */
public record ExceptionResponse(
    /**
     * HTTP status code as a string
     */
    String statusCode, 
    
    /**
     * Brief title describing the error
     */
    String title, 
    
    /**
     * Timestamp when the error occurred
     */
    String timestamp, 
    
    /**
     * Detailed description of the error
     */
    String detail,
    
    /**
     * Request path where the error occurred
     */
    String path, 
    
    /**
     * List of field-specific validation errors
     */
    List<String> fieldErrors) {

    /**
     * Constructor for creating an exception response without field errors.
     *
     * @param statusCode HTTP status code as a string
     * @param title Brief title describing the error
     * @param timestamp Timestamp when the error occurred
     * @param detail Detailed description of the error
     * @param path Request path where the error occurred
     */
    public ExceptionResponse(String statusCode, String title, String timestamp, String detail,
        String path) {
        this(statusCode, title, timestamp, detail, path, new ArrayList<>());
    }
}
