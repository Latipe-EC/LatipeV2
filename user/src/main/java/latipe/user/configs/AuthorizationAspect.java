package latipe.user.configs;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import src.config.annotation.RequiresAuthorization;
import src.config.exception.BadRequestException;
import src.config.exception.ForbiddenException;
import src.config.utils.Constant;

@Aspect
@Component
public class AuthorizationAspect {
    @Before("@annotation(requiresAuthorization)")
    public void checkAuthorization(JoinPoint joinPoint, RequiresAuthorization requiresAuthorization) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        UserDetails user = (UserDetails) request.getAttribute("user");
        if (user == null)
            throw new BadRequestException("User is not authorized");
        if (!user.getAuthorities().stream().anyMatch(x -> x.getAuthority().equals(requiresAuthorization.value()[0]) || x.getAuthority().equals(Constant.ADMIN))) {
            throw new ForbiddenException("Don't have permission to do this!");
        }
    }
}