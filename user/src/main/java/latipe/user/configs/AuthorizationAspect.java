package latipe.user.configs;

import latipe.user.annotations.RequiresAuthorization;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;


@Aspect
@Component
public class AuthorizationAspect {
//    @Before("@annotation(RequiresAuthorization)")
//    public void checkAuthorization(JoinPoint joinPoint, RequiresAuthorization requiresAuthorization) {
//
//    }
}