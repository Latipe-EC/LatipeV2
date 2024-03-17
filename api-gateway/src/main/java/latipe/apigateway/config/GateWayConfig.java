package latipe.apigateway.config;

import latipe.apigateway.throttle.RemoteAddressKeyResolver;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class GateWayConfig {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(GateWayConfig.class);
  private final RemoteAddressKeyResolver remoteAddressKeyResolver;

  @Value("${EUREKA_PORT:8761}")
  private String eurekaPort;

  @Value("${app.eureka-server}")
  private String eurekaServer;

  @Bean
  public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
    LOGGER.info("Configuring routes");
    return builder.routes()
        .route("auth_service", r -> r.path("/api/v1/auth/**")
            .filters(f -> f
                .requestRateLimiter(c -> c.setRateLimiter(redisRateLimiter())
                    .setKeyResolver(remoteAddressKeyResolver)
                ).setResponseHeader("Access-Control-Allow-Origin", "*")
                .setResponseHeader("Access-Control-Allow-Methods", "*")
                .setResponseHeader("Access-Control-Allow-Headers", "*")
                .setResponseHeader("Access-Control-Max-Age", "30")
            )
            .uri("lb://auth-service"))

        .route("user_service", r -> r.path("/api/v1/users/**", "/api/v1/tokens/**")
            .filters(f -> f
                .requestRateLimiter(c -> c.setRateLimiter(redisRateLimiter())
                    .setKeyResolver(remoteAddressKeyResolver)
                ).setResponseHeader("Access-Control-Allow-Origin", "*")
                .setResponseHeader("Access-Control-Allow-Methods", "*")
                .setResponseHeader("Access-Control-Allow-Headers", "*")
                .setResponseHeader("Access-Control-Max-Age", "30"))
            .uri("lb://user-service"))

        .route("cart_service", r -> r.path("/api/v1/carts/**")
            .filters(f -> f
                .requestRateLimiter(c -> c.setRateLimiter(redisRateLimiter())
                    .setKeyResolver(remoteAddressKeyResolver)
                ).setResponseHeader("Access-Control-Allow-Origin", "*")
                .setResponseHeader("Access-Control-Allow-Methods", "*")
                .setResponseHeader("Access-Control-Allow-Headers", "*")
                .setResponseHeader("Access-Control-Max-Age", "30"))
            .uri("lb://cart-service"))

        .route("media_service", r -> r.path("/api/v1/medias/**")
            .filters(f -> f
                .requestRateLimiter(c -> c.setRateLimiter(redisRateLimiter())
                    .setKeyResolver(remoteAddressKeyResolver)
                ))
            .uri("lb://media-service"))

        .route("product_service", r -> r.path("/api/v1/products/**", "/api/v1/categories/**")
            .filters(f -> f
                .requestRateLimiter(c -> c.setRateLimiter(redisRateLimiter())
                    .setKeyResolver(remoteAddressKeyResolver)
                ).setResponseHeader("Access-Control-Allow-Origin", "*")
                .setResponseHeader("Access-Control-Allow-Methods", "*")
                .setResponseHeader("Access-Control-Allow-Headers", "*")
                .setResponseHeader("Access-Control-Max-Age", "30"))
            .uri("lb://product-service"))

        .route("search_service", r -> r.path("/api/v1/search/**")
            .filters(f -> f
                .requestRateLimiter(c -> c.setRateLimiter(redisRateLimiter())
                    .setKeyResolver(remoteAddressKeyResolver)
                ).setResponseHeader("Access-Control-Allow-Origin", "*")
                .setResponseHeader("Access-Control-Allow-Methods", "*")
                .setResponseHeader("Access-Control-Allow-Headers", "*")
                .setResponseHeader("Access-Control-Max-Age", "30"))
            .uri("lb://search-service"))

        .route("store_service", r -> r.path("/api/v1/stores/**", "/api/v1/commissions/**")
            .filters(f -> f
                .requestRateLimiter(c -> c.setRateLimiter(redisRateLimiter())
                    .setKeyResolver(remoteAddressKeyResolver)
                ).setResponseHeader("Access-Control-Allow-Origin", "*")
                .setResponseHeader("Access-Control-Allow-Methods", "*")
                .setResponseHeader("Access-Control-Allow-Headers", "*")
                .setResponseHeader("Access-Control-Max-Age", "30"))
            .uri("lb://store-service"))

        .route("payment_service", r -> r.path("/api/v1/payment/**", "/api/v1/payment-providers/**")
            .filters(f -> f
                .requestRateLimiter(c -> c.setRateLimiter(redisRateLimiter())
                    .setKeyResolver(remoteAddressKeyResolver)
                ).setResponseHeader("Access-Control-Allow-Origin", "*")
                .setResponseHeader("Access-Control-Allow-Methods", "*")
                .setResponseHeader("Access-Control-Allow-Headers", "*")
                .setResponseHeader("Access-Control-Max-Age", "30"))
            .uri("lb://payment-service"))

        .route("rating-service", r -> r.path("/api/v1/ratings/**")
            .filters(f -> f
                .requestRateLimiter(c -> c.setRateLimiter(redisRateLimiter())
                    .setKeyResolver(remoteAddressKeyResolver)
                ).setResponseHeader("Access-Control-Allow-Origin", "*")
                .setResponseHeader("Access-Control-Allow-Methods", "*")
                .setResponseHeader("Access-Control-Allow-Headers", "*")
                .setResponseHeader("Access-Control-Max-Age", "30")
            )
            .uri("lb://rating-service"))

        .route("discovery_server", r -> r.path("/eureka/web")
            .uri("https://eureka:password@%s:%s".formatted(eurekaServer, eurekaPort)))

        .route("discovery_server_static", r -> r.path("/eureka/**")
            .uri("https://eureka:password@%s:%s".formatted(eurekaServer, eurekaPort)))

        .route("discovery_server_static", r -> r.path("/eureka/**")
            .uri("https://eureka:password@%s:%s".formatted(eurekaServer, eurekaPort)))

        .route("orders-service", r -> r.path("/api/v1/orders/**", "/api/v2/orders/**")
            .filters(f -> f.setResponseHeader("Access-Control-Allow-Origin", "*")
                .setResponseHeader("Access-Control-Allow-Methods", "*")
                .setResponseHeader("Access-Control-Allow-Headers", "*")
                .setResponseHeader("Access-Control-Max-Age", "30"))
            .uri("http://localhost:5000"))

        .route("delivery-service", r -> r.path("/api/v1/delivery/**")

            .uri("http://localhost:5005"))

        .route("vouchers-service", r -> r.path("/api/v1/vouchers/**")
            .filters(f -> f.setResponseHeader("Access-Control-Allow-Origin", "*")
                .setResponseHeader("Access-Control-Allow-Methods", "*")
                .setResponseHeader("Access-Control-Allow-Headers", "*")
                .setResponseHeader("Access-Control-Max-Age", "30"))
            .uri("http://localhost:5010"))

        .build();
  }

  @Bean
  public RedisRateLimiter redisRateLimiter() {
    return new RedisRateLimiter(50000, 100000, 10);
  }

}
