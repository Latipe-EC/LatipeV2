package latipe.user.utils;


import org.apache.commons.lang3.RandomStringUtils;

public class GenerateUtils {

    public static String generateRandomUsername() {
        return RandomStringUtils.randomAlphanumeric(9);
    }

}
