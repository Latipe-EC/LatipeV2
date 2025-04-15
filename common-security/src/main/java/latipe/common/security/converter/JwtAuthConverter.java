package latipe.common.security.converter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import latipe.common.security.properties.JwtAuthConverterProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

/**
 * Custom JWT converter to extract authorities (roles/permissions) based on configured properties.
 * It looks for claims specified in JwtAuthConverterProperties and maps them to Spring Security's GrantedAuthority.
 */
@RequiredArgsConstructor
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtAuthConverterProperties properties;

    // Default converter to extract scopes like "SCOPE_read"
    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    /**
     * Converts the JWT into an Authentication token, extracting authorities.
     *
     * @param jwt The decoded JWT.
     * @return An AbstractAuthenticationToken (typically JwtAuthenticationToken) containing authorities.
     */
    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
        Collection<GrantedAuthority> authorities = Stream.concat(
            // Extract standard scopes (e.g., SCOPE_profile)
            jwtGrantedAuthoritiesConverter.convert(jwt).stream(),
            // Extract custom roles/permissions from configured claims
            extractResourceRoles(jwt).stream()
        ).collect(Collectors.toSet());

        return new JwtAuthenticationToken(jwt, authorities, getPrincipalClaimName(jwt));
    }

    /**
     * Extracts the principal name from the JWT. Uses the standard "sub" claim by default,
     * but can be customized if needed.
     *
     * @param jwt The decoded JWT.
     * @return The principal name (usually the user ID).
     */
    private String getPrincipalClaimName(Jwt jwt) {
        // Default to "sub" claim (subject, typically user ID)
        return jwt.getClaimAsString("sub");
        // Or customize based on properties.getPrincipalClaimName() if needed for the principal itself
        // String claimName = Optional.ofNullable(properties.getPrincipalClaimName()).orElse("sub");
        // return jwt.getClaimAsString(claimName);
    }

    /**
     * Extracts authorities from the JWT based on the configured claim name and resource ID.
     *
     * @param jwt The decoded JWT.
     * @return A collection of GrantedAuthority objects.
     */
    private Collection<? extends GrantedAuthority> extractResourceRoles(Jwt jwt) {
        String claimName = properties.getPrincipalClaimName();
        String resourceId = properties.getResourceId();
        String authorityPrefix = properties.getAuthorityPrefix();
        boolean convertToUpperCase = properties.isConvertToUpperCase();

        if (claimName == null) {
            return Collections.emptySet(); // No claim specified
        }

        // Get the top-level claim (e.g., "realm_access", "resource_access", "roles", "permissions")
        Map<String, Object> resourceAccess = jwt.getClaimAsMap(claimName);
        if (resourceAccess == null) {
            // Try getting it as a List if it's not a Map (e.g., a simple list of roles/permissions)
            List<String> claimValues = jwt.getClaimAsStringList(claimName);
            if (claimValues != null) {
                 return claimValues.stream()
                    .map(role -> formatAuthority(role, authorityPrefix, convertToUpperCase))
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toSet());
            }
            return Collections.emptySet(); // Claim not found or not in expected format
        }

        // If resourceId is specified, look for roles/permissions nested under that ID
        final Collection<String> resourceRoles;
        if (resourceId != null && !resourceId.isBlank()) {
            Map<String, Object> resource = (Map<String, Object>) resourceAccess.get(resourceId);
            if (resource == null || !resource.containsKey("roles")) {
                 // Fallback: Check if the top-level claim itself contains the roles directly for this resourceId
                 // This handles cases where claimName = "resource_access" and resourceId = "my-client"
                 // and the structure is { "resource_access": { "my-client": ["ROLE_USER"] } } (incorrect Keycloak structure)
                 // OR { "resource_access": { "my-client": { "roles": ["USER"] } } } (correct Keycloak structure)
                 // We also check if resourceAccess itself might be the list if claimName was specific like 'user-roles'
                 if (resourceAccess.containsKey("roles") && resourceAccess.get("roles") instanceof Collection) {
                     resourceRoles = (Collection<String>) resourceAccess.get("roles");
                 } else if (resourceAccess instanceof Collection) {
                     // If the claim itself is a collection (e.g. "roles": ["ADMIN", "USER"])
                     resourceRoles = (Collection<String>) resourceAccess;
                 }
                 else {
                    return Collections.emptySet(); // No roles found for the specified resourceId
                 }
            } else {
                 // Standard case: Extract roles from the "roles" list under the resourceId
                 resourceRoles = (Collection<String>) resource.get("roles");
            }
        } else {
            // No resourceId specified, assume roles are directly under the claimName (e.g., "realm_access": { "roles": [...] })
            // Or the claim itself is the list (e.g. "roles": ["ADMIN", "USER"])
            if (resourceAccess.containsKey("roles") && resourceAccess.get("roles") instanceof Collection) {
                 resourceRoles = (Collection<String>) resourceAccess.get("roles");
            } else if (resourceAccess instanceof Collection) {
                 resourceRoles = (Collection<String>) resourceAccess;
            }
            else {
                // Maybe the claim is directly a list of strings?
                 List<String> claimValues = jwt.getClaimAsStringList(claimName);
                 if (claimValues != null) {
                     resourceRoles = claimValues;
                 } else {
                    return Collections.emptySet(); // Roles not found or not in expected format
                 }
            }
        }

        // Map the extracted role/permission strings to GrantedAuthority objects
        return resourceRoles.stream()
            .map(role -> formatAuthority(role, authorityPrefix, convertToUpperCase))
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toSet());
    }

    /**
     * Formats the authority string by adding a prefix and optionally converting to uppercase.
     *
     * @param rawAuthority The raw authority string from the JWT claim.
     * @param prefix The prefix to add (e.g., "ROLE_").
     * @param toUpperCase Whether to convert the authority to uppercase.
     * @return The formatted authority string.
     */
    private String formatAuthority(String rawAuthority, String prefix, boolean toUpperCase) {
        String formatted = prefix + rawAuthority;
        return toUpperCase ? formatted.toUpperCase() : formatted;
    }
}
