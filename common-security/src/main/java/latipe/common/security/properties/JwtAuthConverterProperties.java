package latipe.common.security.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for mapping JWT claims to Spring Security authorities.
 * Allows customization via application properties (e.g., application.yml).
 */
@Component
@ConfigurationProperties(prefix = "jwt.auth.converter")
@Validated // Optional: Add validation annotations (e.g., @NotBlank) if needed
@Getter
@Setter
public class JwtAuthConverterProperties {

    /**
     * The name of the claim in the JWT token that contains the roles or permissions.
     * Defaults to "roles". Could be "permissions", "scope", "authorities", etc.
     */
    private String principalClaimName = "roles";

    /**
     * The resource ID or client ID expected in the token's claims.
     * Used to extract resource-specific roles/permissions if they are nested under a client ID.
     * If null or empty, roles/permissions are expected directly under the principalClaimName.
     */
    private String resourceId;

    /**
     * Prefix to add to each authority extracted from the JWT.
     * Common prefixes are "ROLE_" for roles or "SCOPE_" for OAuth scopes.
     * Defaults to "ROLE_". Set to empty string "" if no prefix is desired.
     */
    private String authorityPrefix = "ROLE_";

    /**
     * Whether authorities should be converted to uppercase.
     * Defaults to true.
     */
    private boolean convertToUpperCase = true;

}
