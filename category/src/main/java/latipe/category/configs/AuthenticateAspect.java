package latipe.category.configs;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import latipe.category.controllers.APIClient;
import latipe.category.dtos.TokenDto;
import latipe.category.dtos.UserCredentialDto;
import latipe.category.exceptions.UnauthorizedException;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;


@Aspect
@Component
public class AuthenticateAspect {
    private final APIClient apiClient;
    public AuthenticateAspect(APIClient apiClient) {
        this.apiClient = apiClient;
    }
    @Before("@annotation(latipe.category.annotations.Authenticate)")
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
            request.setAttribute("store", credentialDto);
        } catch (FeignException e) {
            throw new UnauthorizedException(e.getMessage());
        }
    }
    private String getTokenFromRequest() {
        // Get token from request headers
        HttpServletRequest request = ((ServletRequestAttributes) (Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))).getRequest();
        final String requestTokenHeader = request.getHeader("Authorization");
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            return requestTokenHeader.substring(7);
        }
        return null;
    }
}