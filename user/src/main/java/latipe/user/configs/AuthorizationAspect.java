package latipe.user.configs;

import feign.Feign;
import feign.FeignException;
import feign.Logger;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.okhttp.OkHttpClient;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import latipe.user.annotations.RequiresAuthorization;
import latipe.user.exceptions.ForbiddenException;
import latipe.user.exceptions.UnauthorizedException;
import latipe.user.feign.AuthClient;
import latipe.user.request.TokenRequest;
import latipe.user.response.UserCredentialResponse;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
public class AuthorizationAspect {

  private final GateWayProperties gateWayProperties;

  @Before("@annotation(requiresAuthorization)")
  public void checkAuthorization(JoinPoint joinPoint, RequiresAuthorization requiresAuthorization)
      throws UnauthorizedException {
    String token = getTokenFromRequest();
    if (token == null) {
      throw new UnauthorizedException("Unauthorized");
    }
    try {
      AuthClient authClient = Feign.builder()
          .client(new OkHttpClient())
          .encoder(new GsonEncoder())
          .decoder(new GsonDecoder())
          .logLevel(Logger.Level.FULL)
          .target(AuthClient.class,
              "%s:%s/api/v1".formatted(gateWayProperties.getHost(), gateWayProperties.getPort()));
      UserCredentialResponse credential = authClient.getCredential(new TokenRequest(token));

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