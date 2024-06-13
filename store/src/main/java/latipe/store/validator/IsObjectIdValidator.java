package latipe.store.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import latipe.store.annotations.IsObjectId;
import org.bson.types.ObjectId;

public class IsObjectIdValidator implements ConstraintValidator<IsObjectId, String> {

    private String returnMessage;

    @Override
    public void initialize(IsObjectId requiredIfChecked) {
        returnMessage = requiredIfChecked.message();
    }

    @Override
    public boolean isValid(String realValue, ConstraintValidatorContext context) {
        var valid = ObjectId.isValid(realValue);
        if (!valid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(returnMessage).addConstraintViolation();
        }
        return valid;
    }

}
