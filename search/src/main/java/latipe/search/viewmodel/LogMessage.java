package latipe.search.viewmodel;

import static latipe.search.constants.CONSTANTS.REQUEST_ID;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import latipe.search.response.UserCredentialResponse;

public record LogMessage(
    String id,
    String userId,
    String sid,
    String ip,
    String userAgent,
    String requestMethod,
    String methodName,
    String message
) {

    public static LogMessage create(String message, HttpServletRequest request, String methodName) {
        if (request != null) {
            var id = UUID.randomUUID().toString();

            request.setAttribute(REQUEST_ID, id);
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null) {
                ip = request.getRemoteAddr();
            }
            return new LogMessage(id, getUserId(request), request.getHeader("Sid"), ip,
                request.getHeader("User-Agent"),
                request.getMethod(), methodName, message);
        } else {
            return new LogMessage("", "", "", "", "", "",
                methodName, message);
        }
    }

    private static String getUserId(HttpServletRequest request) {
        if (request == null) {
            return "";
        }

        var u = request.getAttribute("user");
        return u == null ? "" : ((UserCredentialResponse) u).id();
    }
}
