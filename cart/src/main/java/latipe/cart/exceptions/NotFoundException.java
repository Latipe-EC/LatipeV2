package latipe.cart.exceptions;


import latipe.cart.utils.MessagesUtils;

/**
 * Exception thrown when a requested resource cannot be found.
 * Corresponds to HTTP status code 404 (Not Found).
 */
public class NotFoundException extends RuntimeException {

    private final String message;

    /**
     * Constructs a new NotFoundException with a message derived from an error code.
     *
     * @param errorCode The message key to look up in the message resource bundle.
     * @param var2 Optional arguments for message formatting.
     */
    public NotFoundException(String errorCode, Object... var2) {
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
