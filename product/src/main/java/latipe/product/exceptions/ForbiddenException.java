package latipe.product.exceptions;


import latipe.product.utils.MessagesUtils;

/**
 * Exception thrown when a user attempts an action they do not have permission for.
 * Corresponds to HTTP status code 403 (Forbidden).
 */
public class ForbiddenException extends RuntimeException {

    private final String message;

    /**
     * Constructs a new ForbiddenException with a message derived from an error code.
     *
     * @param errorCode The message key to look up in the message resource bundle.
     * @param var2 Optional arguments for message formatting.
     */
    public ForbiddenException(String errorCode, Object... var2) {
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
