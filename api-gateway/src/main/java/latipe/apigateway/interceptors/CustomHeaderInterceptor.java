package latipe.apigateway.interceptors;


import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import latipe.apigateway.Utils.Utils;
import latipe.apigateway.constants.Const;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomHeaderInterceptor implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        if (httpServletResponse.getHeaderNames().contains(Const.SESSION_ID)) {
            chain.doFilter(request, response);
            return;
        }

        httpServletResponse.setHeader(Const.SESSION_ID,
            Utils.encodeSession(Utils.getRealIp((HttpServletRequest) request)));
        httpServletResponse.setHeader(Const.ANONYMOUS, "1");

        log.info("Session ID: {}", httpServletResponse.getHeader(Const.SESSION_ID));

        chain.doFilter(request, response);
    }
}
