package latipe.apigateway.config;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import latipe.apigateway.config.RoutesDefine.RouteDefine;
import latipe.apigateway.throttle.RemoteAddressKeyResolver;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

@Configuration
@RequiredArgsConstructor
public class GateWayConfig {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(GateWayConfig.class);
  private final RemoteAddressKeyResolver remoteAddressKeyResolver;

  @Value("${gateway_routes}")
  private String routeDefinitionsFilePath;

  private final Gson gson;

  @Bean
  public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
    LOGGER.info("Configuring routes");

    Yaml yaml = new Yaml(new Constructor(Map.class));
    RoutesDefine routesDefine;
    try {
      Map<String, Object> yamlMap = yaml.load(new FileReader(routeDefinitionsFilePath));
      String json = gson.toJson(yamlMap);
      routesDefine = gson.fromJson(json, RoutesDefine.class);

    } catch (IOException e) {
      throw new RuntimeException("Failed to read route definitions file", e);
    }

    var routes = builder.routes();
    for (RouteDefine routeDefine : routesDefine.getRoutes()) {
      routes.route(routeDefine.getId(), r -> r.path(routeDefine.getPaths())
          .filters(f -> f
              .requestRateLimiter(c -> c.setRateLimiter(redisRateLimiter())
                  .setKeyResolver(remoteAddressKeyResolver)
              ).setResponseHeader("Access-Control-Allow-Origin", "*")
              .setResponseHeader("Access-Control-Allow-Methods", "*")
              .setResponseHeader("Access-Control-Allow-Headers", "*")
              .setResponseHeader("Access-Control-Max-Age", "30")
          )
          .uri(routeDefine.getUri()));
    }

    return routes.build();
  }

  @Bean
  public RedisRateLimiter redisRateLimiter() {
    return new RedisRateLimiter(50000, 100000, 10);
  }

}
