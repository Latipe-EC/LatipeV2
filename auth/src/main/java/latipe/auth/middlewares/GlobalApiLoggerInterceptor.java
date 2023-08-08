package latipe.auth.middlewares;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.InetAddress;
import java.util.UUID;

@Component
public class GlobalApiLoggerInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(GlobalApiLoggerInterceptor.class);

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        if (request.getRequestURI().contains("api")) {
            String requestId = (String) request.getAttribute("requestId");
            String requestIP = (String) request.getAttribute("requestIP");
            String responseStatus = String.valueOf(response.getStatus());
            String responseTime = String.valueOf(System.currentTimeMillis() - (Long) request.getAttribute("startTime"));
            logger.info("-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
            logger.info("Request ID: " + "\u001B[32m" + "{}" + "\u001B[0m", requestId);
            logger.info("Request IP: {}", requestIP);
            logger.info("Request Method: " + "\u001B[32m" + "{}" + "\u001B[0m", request.getMethod());
            logger.info("Response Status: {}", responseStatus);
            logger.info("Response Time: {} ms", responseTime);
            // Add response after if have time
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (request.getRequestURI().contains("api")) {
            request.setAttribute("requestId", UUID.randomUUID().toString());
            request.setAttribute("requestIP", request.getRemoteAddr().equalsIgnoreCase("0:0:0:0:0:0:0:1") ? InetAddress.getLocalHost().getHostAddress() : request.getRemoteAddr());
            request.setAttribute("startTime", System.currentTimeMillis());
        }
        return true;
    }
}