package latipe.apigateway.config;
import latipe.apigateway.Utils.Utils;
import latipe.apigateway.constants.Const;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class CustomGatewayFilter implements GatewayFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Pre-processing logic here
        System.out.println("Pre-processing logic in CustomGatewayFilter");

        return chain.filter(exchange)
            .then(Mono.fromRunnable(() -> {
                if (exchange.getRequest().getHeaders().get(Const.SESSION_ID) == null) {
                    // Post-processing logic here
                    exchange.getResponse().getHeaders().add(Const.SESSION_ID
                        , Utils.encodeSession(Utils.getRealIp(exchange.getRequest())));
                    exchange.getRequest().getHeaders().add(Const.SESSION_ID
                        , Utils.encodeSession(Utils.getRealIp(exchange.getRequest())));
                }
            }));
    }
}
