package latipe.apigateway.config;

import com.google.gson.Gson;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import latipe.apigateway.throttle.AuthorizationKeyResolver;
import latipe.apigateway.throttle.RemoteAddressKeyResolver;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

@Configuration
@RequiredArgsConstructor
public class GateWayConfig {

    // Logger for logging information and errors
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(GateWayConfig.class);

    // Resolver for getting the remote address for rate limiting
    private final RemoteAddressKeyResolver remoteAddressKeyResolver;
    // Gson for converting JSON to Java objects and vice versa
    private final Gson gson;
    // Resolver for getting the authorization key for rate limiting
    private final AuthorizationKeyResolver authorizationKeyResolver;
    // Path to the file containing the route definitions
    @Value("${gateway_routes}")
    private String routeDefinitionsFilePath;

    // Bean for creating the route locator
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        // Log the start of route configuration
        LOGGER.info("Starting route configuration");

        // Load the route definitions from the file
        var routesDefine = loadRouteDefinitions();

        // Builder for creating the routes
        var routes = builder.routes();
        for (var routeDefine : routesDefine.getRoutes()) {
            // Log the start of configuration for each route
            LOGGER.info("Configuring route: {}", routeDefine.getId());

            // Configure each route with its path, filters, and URI
            routes.route(routeDefine.getId(), r -> r.path(routeDefine.getPaths())
                .filters(f -> configureFilters(f, routeDefine))
                .uri(routeDefine.getUri()));

            // Log the end of configuration for each route
            LOGGER.info("Finished configuring route: {}", routeDefine.getId());
        }

        // Log the end of route configuration
        LOGGER.info("Finished route configuration");

        // Build and return the route locator
        return routes.build();
    }

    // Method for loading the route definitions from the file
    private RoutesDefine loadRouteDefinitions() {
        // Log the start of loading route definitions
        LOGGER.info("Loading route definitions from file: {}", routeDefinitionsFilePath);

        // Yaml for parsing the YAML file
        Yaml yaml = new Yaml(new Constructor(Map.class));
        try {
            // Load the YAML file into a map
            Map<String, Object> yamlMap = yaml.load(new FileReader(routeDefinitionsFilePath));

            // Convert the map to JSON
            String json = gson.toJson(yamlMap);

            // Convert the JSON to a RoutesDefine object
            RoutesDefine routesDefine = gson.fromJson(json, RoutesDefine.class);

            // Log the successful loading of route definitions
            LOGGER.info("Successfully loaded route definitions");

            // Return the RoutesDefine object
            return routesDefine;
        } catch (IOException e) {
            // Log any errors that occur while reading the file
            LOGGER.error("Failed to read route definitions file", e);

            // Throw a runtime exception if the file cannot be read
            throw new RuntimeException("Failed to read route definitions file", e);
        }
    }

    // Method for configuring the filters for a route
    private GatewayFilterSpec configureFilters(GatewayFilterSpec f,
        RoutesDefine.RouteDefine routeDefine) {
        // Check if the route has any filters
        if (routeDefine.getFilter() != null) {
            // Log the start of filter configuration
            LOGGER.info("Configuring filters for route: {}", routeDefine.getId());

            // Check if the route has a request rate limiter
            if (routeDefine.getFilter().getRequestRateLimiter() != null) {
                // Log the start of request rate limiter configuration
                LOGGER.info("Configuring request rate limiter for route: {}", routeDefine.getId());

                // Configure the request rate limiter
                f.requestRateLimiter().rateLimiter(RedisRateLimiter.class,
                        c -> c.setBurstCapacity(
                                routeDefine.getFilter().getRequestRateLimiter().getBurstCapacity())
                            .setReplenishRate(
                                routeDefine.getFilter().getRequestRateLimiter().getReplenishRate())
                            .setReplenishRate(
                                routeDefine.getFilter().getRequestRateLimiter().getReplenishRate())
                    )
                    .configure(c -> c.setKeyResolver(authorizationKeyResolver)
                        .setKeyResolver(remoteAddressKeyResolver)).retry(3);
            }

            // Check if the route has any response headers
            if (routeDefine.getFilter().getResponseHeaders() != null) {
                // Log the start of response header configuration
                LOGGER.info("Configuring response headers for route: {}", routeDefine.getId());

                // Configure the response headers
                for (var responseHeader : routeDefine.getFilter().getResponseHeaders()) {
                    for (Map.Entry<String, String> entry : responseHeader.entrySet()) {
                        f.setResponseHeader(entry.getKey(), entry.getValue());
                    }
                }
            }
        }

        // Return the GatewayFilterSpec object
        return f;
    }
}