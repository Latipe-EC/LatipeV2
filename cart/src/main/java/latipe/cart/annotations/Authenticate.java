package latipe.cart.annotations;


import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that indicates a method requires authentication.
 * Used in conjunction with AuthenticateAspect to enforce authentication for API endpoints.
 * 
 * <p>This annotation is processed at runtime by an aspect that verifies the JWT token
 * in the request header before allowing the method execution.</p>
 * 
 * <p>It also adds a Swagger UI security requirement, indicating that the endpoint
 * needs Bearer Authentication in the API documentation.</p>
 * 
 * <p>Example usage:</p>
 * <pre>
 * {@code @Authenticate}
 * {@code @GetMapping("/cart")}
 * public ResponseEntity<?> getCart() {
 *     // Only authenticated users can access this endpoint
 *     // The user credential is available via request.getAttribute("user")
 * }
 * </pre>
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@SecurityRequirement(name = "Bearer Authentication")
public @interface Authenticate {

}

