package latipe.user.configs;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import latipe.user.controllers.APIClient;
import latipe.user.exceptions.UnauthorizedException;
import latipe.user.request.TokenRequest;
import latipe.user.response.UserCredentialResponse;
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
    @Before("@annotation(latipe.user.annotations.Authenticate)")
    public void authenticate() throws UnauthorizedException {
        String token = getTokenFromRequest();
        if (token == null) {
            throw new UnauthorizedException("Unauthorized");
        }
        try {
            UserCredentialResponse userCredential =  apiClient.getCredential(new TokenRequest(token));
            if (userCredential == null) {
                throw new UnauthorizedException("Unauthorized");
            }
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            request.setAttribute("user", userCredential);
        } catch (FeignException e) {
            throw new UnauthorizedException(e.getMessage());
        }
    }

    private String getTokenFromRequest() {
        // Get token from request headers
        HttpServletRequest request = ((ServletRequestAttributes) (RequestContextHolder.getRequestAttributes())).getRequest();
        final String requestTokenHeader = request.getHeader("Authorization");
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            return requestTokenHeader.substring(7);
        }
        return null;
    }
}
