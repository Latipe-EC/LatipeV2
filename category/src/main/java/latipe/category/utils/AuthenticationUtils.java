package latipe.category.utils;

public class AuthenticationUtils {
    private AuthenticationUtils() {
        // Private constructor to prevent instantiation
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
