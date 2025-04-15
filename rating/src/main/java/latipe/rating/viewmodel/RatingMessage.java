package latipe.rating.viewmodel;

import lombok.Builder;

/**
 * Record representing a rating event message.
 * Used for transmitting rating-related events between microservices.
 */
@Builder
public record RatingMessage(
    /**
     * ID of the order item being rated
     */
    String orderItemId, 
    
    /**
     * ID of the product being rated
     */
    String productId, 
    
    /**
     * ID of the rating entity
     */
    String ratingId, 
    
    /**
     * The numeric rating value
     */
    Integer rating,
    
    /**
     * ID of the store associated with the rated product
     */
    String storeId,
    
    /**
     * Previous rating value (used for updates)
     */
    Integer oldRating,
    
    /**
     * Operation type (create, update, delete)
     */
    String op) {

}

