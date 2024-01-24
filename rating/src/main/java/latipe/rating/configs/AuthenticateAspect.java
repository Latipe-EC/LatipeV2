package latipe.rating.configs;

import feign.Feign;
import feign.FeignException;
import feign.Logger;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.okhttp.OkHttpClient;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import latipe.rating.exceptions.UnauthorizedException;
import latipe.rating.feign.AuthClient;
import latipe.rating.request.TokenRequest;
import latipe.rating.response.UserCredentialResponse;
import lombok.AllArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


@Aspect
@Component
@AllArgsConstructor
public class AuthenticateAspect {

  private final GateWayProperties gateWayProperties;

  @Before("@annotation(latipe.rating.annotations.Authenticate)")
  public void authenticate() throws UnauthorizedException {
    AuthClient authClient = Feign.builder()
        .client(new OkHttpClient())
        .encoder(new GsonEncoder())
        .decoder(new GsonDecoder())
        .logLevel(Logger.Level.FULL)
        .target(AuthClient.class,
            "%s:%s/api/v1".formatted(gateWayProperties.getHost(), gateWayProperties.getPort()));
    String token = getTokenFromRequest();
    if (token == null) {
      throw new UnauthorizedException("Unauthorized");
    }
    try {
      UserCredentialResponse credential = authClient.getCredential(new TokenRequest(token));
      if (credential == null) {
        throw new UnauthorizedException("Unauthorized");
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
