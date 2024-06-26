package latipe.rating.utils;

import jakarta.servlet.http.HttpServletRequest;
import latipe.rating.exceptions.NotFoundException;

public class GetBearerToken {

    public static String get(HttpServletRequest request) {
        String requestTokenHeader = request.getHeader("Authorization");
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            return requestTokenHeader;
        }
        throw new NotFoundException("Token not found");
    }

}
