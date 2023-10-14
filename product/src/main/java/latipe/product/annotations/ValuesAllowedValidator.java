package latipe.product.annotations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;

public class ValuesAllowedValidator implements ConstraintValidator<ValuesAllow, String> {

  private List<String> expectedValues;
  private String returnMessage;

  @Override
  public void initialize(ValuesAllow requiredIfChecked) {
    expectedValues = Arrays.asList(requiredIfChecked.values());
    returnMessage = requiredIfChecked.message().concat(expectedValues.toString());
  }

  @Override
  public boolean isValid(String realValue, ConstraintValidatorContext context) {
    var valid = expectedValues.contains(realValue);
    if (!valid) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(returnMessage).addConstraintViolation();
    }
    return valid;
  }

}
