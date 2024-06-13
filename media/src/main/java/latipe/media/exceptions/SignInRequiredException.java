package latipe.media.exceptions;


import latipe.media.utils.MessagesUtils;

public class SignInRequiredException extends RuntimeException {

    private final String message;

    public SignInRequiredException(String errorCode, Object... var2) {
        this.message = MessagesUtils.getMessage(errorCode, var2);
    }

    @Override
    public String getMessage() {
        return message;
    }

}
