package latipe.product.configs;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import latipe.product.annotations.RequiresAuthorization;
import latipe.product.controllers.APIClient;
import latipe.product.dtos.TokenDto;
import latipe.product.dtos.UserCredentialDto;
import latipe.product.exceptions.ForbiddenException;
import latipe.product.exceptions.UnauthorizedException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;


@Aspect
@Component
public class AuthorizationAspect {
    private final APIClient apiClient;
    public AuthorizationAspect(APIClient apiClient) {
        this.apiClient = apiClient;
    }
    @Before("@annotation(requiresAuthorization)")
    public void checkAuthorization(JoinPoint joinPoint, RequiresAuthorization requiresAuthorization) throws UnauthorizedException {
        String token = getTokenFromRequest();
        if (token == null) {
            throw new UnauthorizedException("Unauthorized");
        }
        try {
            UserCredentialDto credentialDto =  apiClient.getCredential(new TokenDto(token));
            if (credentialDto == null) {
                throw new UnauthorizedException("Unauthorized");
            }
            if (!credentialDto.getRole().equals(requiresAuthorization.value()[0])) {
                throw new ForbiddenException("Don't have permission to do this!");
            }
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            request.setAttribute("user", credentialDto);
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