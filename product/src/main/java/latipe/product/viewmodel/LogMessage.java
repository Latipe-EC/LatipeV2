package latipe.product.viewmodel;

import static latipe.product.constants.CONSTANTS.REQUEST_ID;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;

public record LogMessage(
    String id,
    String ip,
    String userAgent,
    String requestMethod,
    String methodName,
    String message
) {

  public static LogMessage create(String message, HttpServletRequest request, String methodName) {
    var id = UUID.randomUUID().toString();
    request.setAttribute(REQUEST_ID, id);
    return new LogMessage(id, request.getRemoteAddr(),
        request.getHeader("User-Agent"),
        request.getMethod(), methodName, message);
  }
}
