package latipe.apigateway.config;

import java.util.Map;
import lombok.Data;

// This class represents the route definitions for the API gateway
@Data
public class RoutesDefine {

    // An array of route definitions
    RouteDefine[] routes;

    // This class represents a single route definition
    @Data
    public static class RouteDefine {

        // The ID of the route
        private String id;

        // The paths that this route should match
        private String[] paths;

        // The filter to apply to this route
        private Filter filter;

        // The URI to forward the request to when this route is matched
        private String uri;
    }

    // This class represents a rate limiter configuration
    @Data
    public static class RateLimiter {

        // The number of tokens to add to the token bucket every second
        private int replenishRate;

        // The maximum number of tokens that the token bucket can hold
        private int burstCapacity;

        // The number of tokens to request from the token bucket for each request
        private int requestedTokens;
    }

    // This class represents a filter configuration
    @Data
    public static class Filter {

        // The rate limiter to apply to this filter
        private RateLimiter requestRateLimiter;

        // The response headers to add when this filter is applied
        private Map<String, String>[] responseHeaders;
    }
}