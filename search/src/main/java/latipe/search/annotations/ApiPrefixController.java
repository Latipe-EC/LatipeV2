package latipe.search.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RequestMapping("/api${path}")
public @interface ApiPrefixController {

    @AliasFor(annotation = RequestMapping.class, attribute = "path")
    String[] value() default {};

    @AliasFor(annotation = RequestMapping.class, attribute = "method")
    RequestMethod[] method() default {};
}