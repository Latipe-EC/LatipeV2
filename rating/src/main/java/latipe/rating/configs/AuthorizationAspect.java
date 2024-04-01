package latipe.rating.configs;

import feign.Feign;
import feign.FeignException;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.okhttp.OkHttpClient;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import latipe.rating.annotations.RequiresAuthorization;
import latipe.rating.exceptions.ForbiddenException;
import latipe.rating.exceptions.UnauthorizedException;
import latipe.rating.feign.AuthClient;
import latipe.rating.request.TokenRequest;
import latipe.rating.utils.GetInstanceServer;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


@Aspect
@RequiredArgsConstructor
public class AuthorizationAspect {

  private final LoadBalancerClient loadBalancer;
  private final GsonDecoder gsonDecoder;
  private final GsonEncoder gsonEncoder;
  private final OkHttpClient okHttpClient;

  @Value("${service.auth}")
  private String authService;

  @Value("${eureka.client.enabled}")
  private boolean useEureka;

  @Before("@annotation(requiresAuthorization)")
  public void checkAuthorization(JoinPoint joinPoint, RequiresAuthorization requiresAuthorization)
      throws UnauthorizedException {
    String token = getTokenFromRequest();
    if (token == null) {
      throw new UnauthorizedException("Unauthorized");
    }
    try {

      var authClient = Feign.builder().client(okHttpClient).encoder(gsonEncoder)
          .decoder(gsonDecoder).target(AuthClient.class,
              useEureka ? String.format("%s/api/v1", GetInstanceServer.get(
                  loadBalancer, authService
              )) : authService);
      var credential = authClient.getCredential(new TokenRequest(token));

      if (credential == null) {
        throw new UnauthorizedException("Unauthorized");
      }
      if (!credential.role().equals(requiresAuthorization.value()[0])) {
        throw new ForbiddenException("Don't have permission to do this!");
      }
      HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
      request.setAttribute("user", credential);
    } catch (FeignException e) {
      throw new UnauthorizedException(e.getMessage());
    }
  }

  private String getTokenFromRequest() {
    // Get token from request headers
    HttpServletRequest request = ((ServletRequestAttributes) (Objects.requireNonNull(
        RequestContextHolder.getRequestAttributes()))).getRequest();
    final String requestTokenHeader = request.getHeader("Authorization");
    if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
      return requestTokenHeader.substring(7);
    }
    return null;
  }
}