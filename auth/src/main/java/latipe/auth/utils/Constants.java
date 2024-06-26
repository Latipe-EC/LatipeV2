package latipe.auth.utils;

public final class Constants {

    public static final class ErrorCode {

        public static final String SIGN_IN_REQUIRED = "SIGN_IN_REQUIRED";
        public static final String FORBIDDEN = "FORBIDDEN";
        public static final String NOT_FOUND = "NOT_FOUND";
        public static final String TOKEN_EXPIRED = "TOKEN_EXPIRED";
        public static final String TOKEN_INVALID = "TOKEN_INVALID";

        // Private constructor to prevent instantiation
        private ErrorCode() {
        }
    }

}
