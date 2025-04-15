package latipe.user.exceptions;


import latipe.user.utils.MessagesUtils;

/**
 * Exception thrown when an operation requires user authentication, but the user is not signed in.
 * Often corresponds to HTTP status code 401 (Unauthorized), specifically indicating the need to log in.
 */
public class SignInRequiredException extends RuntimeException {

    private final String message;

    /**
     * Constructs a new SignInRequiredException with a message derived from an error code.
     *
     * @param errorCode The message key to look up in the message resource bundle.
     * @param var2 Optional arguments for message formatting.
     */
    public SignInRequiredException(String errorCode, Object... var2) {
        this.message = MessagesUtils.getMessage(errorCode, var2);
    }

    /**
     * Returns the detail message string of this throwable.
     *
     * @return the detail message string of this {@code Throwable} instance.
     */
    @Override
    public String getMessage() {
        return message;
    }

}
