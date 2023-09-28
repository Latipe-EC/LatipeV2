package latipe.store.exceptions;


import latipe.store.utils.MessagesUtils;

public class UnauthorizedException extends RuntimeException {

  private final String message;

  public UnauthorizedException(String errorCode, Object... var2) {
    this.message = MessagesUtils.getMessage(errorCode, var2);
  }

  @Override
  public String getMessage() {
    return message;
  }

}
