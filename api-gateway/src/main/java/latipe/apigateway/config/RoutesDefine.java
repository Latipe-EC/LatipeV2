package latipe.apigateway.config;

import java.util.Map;
import lombok.Data;

@Data
public class RoutesDefine {
  RouteDefine[] routes;

  @Data
  public static class RouteDefine {
    private String id;
    private String[] paths;
    private Filter filter;
    private String uri;

  }

  @Data
  public static class RateLimiter {
    private int replenishRate;
    private int burstCapacity;
    private int requestedTokens;
  }

  @Data
  public static class Filter {
    private RateLimiter requestRateLimiter;
    private Map<String, String>[] responseHeaders;
  }

  @Data
  public static class ResponseHeader {
    private String key;
    private String value;
  }
}

