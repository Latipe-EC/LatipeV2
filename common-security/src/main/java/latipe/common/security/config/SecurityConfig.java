package latipe.common.security.config;

import latipe.common.security.converter.JwtAuthConverter;
import latipe.common.security.properties.JwtAuthConverterProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Centralized Spring Security configuration for microservices acting as Resource Servers.
 * Enables JWT validation and method-level security.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Enables @PreAuthorize, @PostAuthorize, etc.
@EnableConfigurationProperties(JwtAuthConverterProperties.class) // Enable the properties class
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthConverterProperties jwtAuthConverterProperties;

    // Define allowed public paths (actuator, swagger, etc.)
    private static final String[] PUBLIC_PATHS = {
        "/swagger-ui.html",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/swagger-resources/**",
        "/webjars/**",
        "/actuator/**",
        // Add any other public paths specific to your services if needed
        // e.g., "/api/v1/products/public/**"
    };

    /**
     * Configures the main security filter chain.
     * - Disables CSRF as we are using stateless JWT authentication.
     * - Configures JWT validation for the resource server.
     * - Sets session management to STATELESS.
     * - Defines public paths that bypass security.
     * - Requires authentication for all other paths.
     *
     * @param http HttpSecurity configuration object.
     * @return The configured SecurityFilterChain.
     * @throws Exception If configuration fails.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable); // Disable CSRF for stateless APIs

        // Configure JWT resource server validation
        http.oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter()))
        );

        // Set session management to stateless
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Define authorization rules
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers(PUBLIC_PATHS).permitAll() // Allow public access to specified paths
            .anyRequest().authenticated() // Require authentication for all other requests
        );

        return http.build();
    }

    /**
     * Creates the JwtAuthenticationConverter bean, configured with properties.
     * This converter extracts authorities (roles/permissions) from the JWT claims.
     *
     * @return The configured JwtAuthConverter.
     */
    @Bean
    public JwtAuthConverter jwtAuthConverter() {
        return new JwtAuthConverter(jwtAuthConverterProperties);
    }

    // You can add more beans here if needed, e.g., PasswordEncoder if any service needs it locally,
    // or custom AccessDeniedHandler/AuthenticationEntryPoint.
}
