package latipe.apigateway.config;

import lombok.Data;

@Data
public class RoutesDefine {
  RouteDefine[] routes;

  @Data
  public static class RouteDefine {
    String[] paths;
    String id;
    String uri;
  }
}

