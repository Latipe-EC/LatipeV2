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
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
  private final AuthorizationKeyResolver authorizationKeyResolver;

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
    for (var routeDefine : routesDefine.getRoutes()) {
      routes.route(routeDefine.getId(), r -> r.path(routeDefine.getPaths())
          .filters(f -> {
                if (routeDefine.getFilter() != null) {
                  if (routeDefine.getFilter().getRequestRateLimiter() != null) {

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

                    if (routeDefine.getFilter().getResponseHeaders() != null) {
                      for (var responseHeader : routeDefine.getFilter().getResponseHeaders()) {
                        for (Map.Entry<String, String> entry : responseHeader.entrySet()) {
                          f.setResponseHeader(entry.getKey(), entry.getValue());
                        }
                      }
                    }
                  }
                }
                return f;
              }
          ).uri(routeDefine.getUri()));
    }
    return routes.build();
  }

}
