package latipe.auth.utils;

import java.lang.reflect.Field;
import java.util.List;
import latipe.auth.exceptions.BadRequestException;

public class checkClass {

    public static <T> boolean checkFieldsViolation(T obj1, T obj2, List<String> ignoreFields) {
        Class<?> clazz = obj1.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            String fieldName = field.getName();
            if (!ignoreFields.contains(fieldName)) {
                try {
                    Object value1 = field.get(obj1);
                    Object value2 = field.get(obj2);
                    if (value1 != null && !value1.equals(value2)) {
                        throw new BadRequestException("Field %s has been violated.", fieldName);
                    } else if (value1 == null && value2 != null) {
                        throw new BadRequestException("Field %s has been violated.", fieldName);
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e.getMessage());
                }
            }
        }
        return false;
    }
}
