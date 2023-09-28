package latipe.search.exceptions;


import latipe.search.utils.MessagesUtils;

public class ForbiddenException extends RuntimeException {

  private final String message;

  public ForbiddenException(String errorCode, Object... var2) {
    this.message = MessagesUtils.getMessage(errorCode, var2);
  }

  @Override
  public String getMessage() {
    return message;
  }

}
