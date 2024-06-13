/**
 *
 */
package latipe.apigateway.throttle;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.support.ipresolver.XForwardedRemoteAddressResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RemoteAddressKeyResolver implements KeyResolver {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Mono<String> resolve(ServerWebExchange exchange) {
        var resolver = XForwardedRemoteAddressResolver.maxTrustedIndex(1);
        var inetSocketAddress = resolver.resolve(exchange);
        var ipAddress = inetSocketAddress.getAddress().getHostAddress();

        var xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");

        if (xForwardedFor != null) {
            String[] ipAddresses = xForwardedFor.split(",");
            if (ipAddresses.length != 0) {
                ipAddress = ipAddresses[0]; // The real IP address is usually the first one in the list
            }
        }

        logger.info("Ip-Address: " + ipAddress);
        return Mono.just(ipAddress);
    }

    @SuppressWarnings("unused")
    private void checkTime(ServerWebExchange exchange) {
        logger.debug(this.getClass().getSimpleName(),
            Thread.currentThread().getStackTrace()[1].getMethodName());
        try {
            //exchange
            String time = exchange.getRequest().getHeaders().get("Request-Time").get(0);
            System.out.println(generateKey(time));
        } catch (NullPointerException e) {
            logger.error("Null Pointer exception {} ", e.getCause());
        }
    }


    private String generateKey(String date) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(date.getBytes(StandardCharsets.UTF_8));
            return String.format("%032x", new BigInteger(1, bytes));
        } catch (NoSuchAlgorithmException nsae) {
            throw new IllegalStateException(
                "MD5 algorithm not available.  Fatal (should be in the JDK).",
                nsae);
        }
    }


    private void findPattern(ServerWebExchange exchange) {
        String token = exchange.getRequest().getHeaders().get("Authorization").get(0);
        logger.info("Token: " + token.replace("Bearer ", ""));
        String ipAddress = exchange.getRequest().getRemoteAddress().getHostName();
        logger.info("Ip-Address: " + ipAddress);
        int port = exchange.getRequest().getRemoteAddress().getPort();
        logger.info("Port: " + port);
        String method = exchange.getRequest().getMethod().name();
        logger.info("Method: " + method);
        String path = exchange.getRequest().getPath().value();
        logger.info("Path: " + path);
        String userAgent = exchange.getRequest().getHeaders().get("User-Agent").get(0);
        logger.info("User Agent: " + userAgent);
    }

}
