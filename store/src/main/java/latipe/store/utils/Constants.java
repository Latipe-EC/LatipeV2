package latipe.store.utils;

public final class Constants {

  public static final String USER = "USER";
  public static final String VENDOR = "VENDOR";
  public static final String ADMIN = "ADMIN";

  public final class ErrorCode {

    public static final String SIGN_IN_REQUIRED = "SIGN_IN_REQUIRED";
    public static final String FORBIDDEN = "FORBIDDEN";
    public static final String NOT_FOUND = "NOT_FOUND";
    public static final String TOKEN_EXPIRED = "TOKEN_EXPIRED";
    // Private constructor to prevent instantiation
    private ErrorCode() {
    }

  }

}
