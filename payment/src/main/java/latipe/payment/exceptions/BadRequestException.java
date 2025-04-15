package latipe.payment.exceptions;


import latipe.payment.utils.MessagesUtils;

/**
 * Exception thrown when a client sends a malformed request.
 * Corresponds to HTTP status code 400 (Bad Request).
 */
public class BadRequestException extends RuntimeException {

    private final String message;

    /**
     * Constructs a new BadRequestException with a message derived from an error code.
     *
     * @param errorCode The message key to look up in the message resource bundle.
     * @param var2 Optional arguments for message formatting.
     */
    public BadRequestException(String errorCode, Object... var2) {
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
