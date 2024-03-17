package latipe.auth.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AuthenticationUtils {

  public static String getMethodName() {
    String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
    if (methodName.startsWith("lambda$")) {
      return methodName.split("\\$")[1];
    }
    return methodName;
  }

  public static String genKeyCacheToken(String token, String userId) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
      StringBuilder hexString = new StringBuilder();
      for (byte b : hash) {
        String hex = Integer.toHexString(0xff & b);
        if (hex.length() == 1) {
          hexString.append('0');
        }
        hexString.append(hex);
      }
      return "%s+%s".formatted(hexString.toString(), userId);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
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
