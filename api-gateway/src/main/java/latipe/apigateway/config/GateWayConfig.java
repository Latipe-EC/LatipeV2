package latipe.apigateway.config;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration

public class GateWayConfig {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(GateWayConfig.class);

  @Value("${app.host}")
  private String host;

  @Value("${server.port}")
  private String port;

  @Bean
  public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
    LOGGER.info("Start config rate limiter");

    return builder.routes()
        .route(r -> r.path("/api/**")
            .filters(f -> f.requestRateLimiter(c -> c.setRateLimiter(redisRateLimiter())))
            .uri("%s:%s".formatted(host, port)))
        .build();
  }

  @Bean
  RedisRateLimiter redisRateLimiter() {
    return new RedisRateLimiter(5, 10);
  }

}
