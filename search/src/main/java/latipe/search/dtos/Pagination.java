package latipe.search.dtos;

import lombok.Data;

/**
 * Data transfer object for pagination information.
 * Used to handle pagination across the application when returning lists of results.
 */
@Data
public class Pagination {

    /**
     * Total number of items available
     */
    private long total;
    
    /**
     * Number of items to skip (offset)
     */
    private long skip;
    
    /**
     * Maximum number of items to return per page
     */
    private int limit;

    /**
     * Creates a new pagination instance with specified parameters.
     * 
     * @param total Total number of available items
     * @param skip Number of items to skip
     * @param limit Maximum number of items per page
     */
    public Pagination(long total, long skip, int limit) {
        this.total = total;
        this.skip = skip;
        this.limit = limit;
    }

    /**
     * Factory method to create a pagination instance.
     * 
     * @param total Total number of available items
     * @param skip Number of items to skip
     * @param limit Maximum number of items per page
     * @return A new Pagination instance
     */
    public static Pagination create(long total, long skip, int limit) {
        return new Pagination(total, skip, limit);
    }

}


