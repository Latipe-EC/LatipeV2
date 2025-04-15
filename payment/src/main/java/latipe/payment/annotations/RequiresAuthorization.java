package latipe.payment.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation to specify authorization requirements for methods.
 * Methods annotated with this require specific permissions to be accessed.
 * 
 * @author Latipe Development Team
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresAuthorization {

    /**
     * Array of permission keys required to access the annotated method.
     * 
     * @return Array of required permission identifiers
     */
    String[] value();
}