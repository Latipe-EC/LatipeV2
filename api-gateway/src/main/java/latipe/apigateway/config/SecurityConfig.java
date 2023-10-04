package latipe.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig implements WebMvcConfigurer {

  @Bean
  public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity serverHttpSecurity) {
    serverHttpSecurity.authorizeExchange(
            exchange -> exchange.pathMatchers("/**").permitAll().anyExchange().authenticated())
        .csrf(ServerHttpSecurity.CsrfSpec::disable).cors(Customizer.withDefaults());
    return serverHttpSecurity.build();
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**").allowedOrigins("*").allowedMethods("*").allowedHeaders("*")
        .exposedHeaders("*");
  }
}
