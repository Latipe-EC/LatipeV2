package latipe.rating.utils;

public class AuthenticationUtils {

    private AuthenticationUtils() {
        // Private constructor to prevent instantiation
    }

    public static String getMethodName() {
        String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        if (methodName.startsWith("lambda$")) {
            return methodName.split("\\$")[1];
        }
        return methodName;
    }
//    public static String getCurrentUserId() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        if (authentication instanceof AnonymousAuthenticationToken) {
//            throw new SignInRequiredException(Constants.ErrorCode.SIGN_IN_REQUIRED);
//        }
//
//        JwtAuthenticationToken contextHolder = (JwtAuthenticationToken) authentication;
//
//        return contextHolder.getToken().getTokenValue();
//    }
}
