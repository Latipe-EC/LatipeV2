/**
 *
 */
package latipe.apigateway.throttle;

import latipe.apigateway.Utils.Utils;
import latipe.apigateway.constants.Const;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Primary

public class AuthorizationKeyResolver implements KeyResolver {

    /* (non-Javadoc)
     * @see org.springframework.cloud.gateway.filter.ratelimit.KeyResolver#resolve(org.springframework.web.server.ServerWebExchange)
     */
    @Override
    public Mono<String> resolve(ServerWebExchange exchange) {
        //exchange.getRequest().getHeaders();
        // TODO Block the user
        //System.out.println(HttpHeaders.AUTHORIZATION);

        var sid = exchange.getRequest().getHeaders().getFirst(Const.SESSION_ID);

        if (sid != null) {
            if (!Utils.validSid(sid)) {
                throw new RuntimeException("Unknown error");
            }
        }

        return sid != null ? Mono.just(sid) : Mono.just(HttpHeaders.AUTHORIZATION);
    }

}
