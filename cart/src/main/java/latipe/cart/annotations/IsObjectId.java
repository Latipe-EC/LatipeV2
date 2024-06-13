package latipe.cart.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import latipe.cart.validator.IsObjectIdValidator;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {IsObjectIdValidator.class})
public @interface IsObjectId {

    String message() default "field-value-should-be-object-id";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
