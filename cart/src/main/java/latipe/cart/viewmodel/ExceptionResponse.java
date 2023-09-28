package latipe.cart.viewmodel;

import java.util.ArrayList;
import java.util.List;

public record ExceptionResponse(String statusCode, String title, String timestamp, String detail,
                                String path, List<String> fieldErrors) {

  public ExceptionResponse(String statusCode, String title, String timestamp, String detail,
      String path) {
    this(statusCode, title, timestamp, detail, path, new ArrayList<>());
  }
}
