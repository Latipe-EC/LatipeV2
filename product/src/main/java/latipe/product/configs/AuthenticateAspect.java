package latipe.product.configs;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import latipe.product.controllers.APIClient;
import latipe.product.dtos.TokenDto;
import latipe.product.dtos.UserCredentialDto;
import latipe.product.exceptions.UnauthorizedException;
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
    @Before("@annotation(latipe.product.annotations.Authenticate)")
    public void authenticate() throws UnauthorizedException {
        String token = getTokenFromRequest();
        if (token == null) {
            throw new UnauthorizedException("Unauthorized");
        }
        try {
            UserCredentialDto credentialDto =  apiClient.getCredential(new TokenDto(token));
            if (credentialDto == null) {
                throw new UnauthorizedException("Unauthorized");
            }
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            request.setAttribute("user", credentialDto);
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