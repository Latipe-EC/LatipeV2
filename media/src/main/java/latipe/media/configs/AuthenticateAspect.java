package latipe.media.configs;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import latipe.media.controllers.APIClient;
import latipe.media.exceptions.UnauthorizedException;
import latipe.media.request.TokenRequest;
import latipe.media.response.UserCredentialResponse;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


@Aspect
@Component
public class AuthenticateAspect {

  private final APIClient apiClient;

  public AuthenticateAspect(APIClient apiClient) {
    this.apiClient = apiClient;
  }

  @Before("@annotation(latipe.media.annotations.Authenticate)")
  public void authenticate() throws UnauthorizedException {
    String token = getTokenFromRequest();
    if (token == null) {
      throw new UnauthorizedException("Unauthorized");
    }
    try {
      UserCredentialResponse credential = apiClient.getCredential(new TokenRequest(token));
      if (credential == null) {
        throw new UnauthorizedException("Unauthorized");
      }
      HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
      request.setAttribute("store", credential);
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
