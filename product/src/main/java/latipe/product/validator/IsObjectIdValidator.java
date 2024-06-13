package latipe.product.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import latipe.product.annotations.IsObjectId;
import org.bson.types.ObjectId;

public class IsObjectIdValidator implements ConstraintValidator<IsObjectId, String> {

    Boolean allowNull;
    private String returnMessage;

    @Override
    public void initialize(IsObjectId requiredIfChecked) {
        returnMessage = requiredIfChecked.message();
        allowNull = requiredIfChecked.allowNull();
    }

    @Override
    public boolean isValid(String realValue, ConstraintValidatorContext context) {
        if (allowNull) {
            return true;
        }
        var valid = ObjectId.isValid(realValue);
        if (!valid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(returnMessage).addConstraintViolation();
        }
        return valid;
    }

}
