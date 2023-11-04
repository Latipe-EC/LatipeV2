package latipe.store.configs;

import jakarta.servlet.http.HttpServletRequest;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import latipe.store.exceptions.UnauthorizedException;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class SecureInternalPhaseAspect {

  @Value("${secure-internal.public-key}")
  private String key;

  @Before("@annotation(latipe.store.annotations.SecureInternalPhase)")
  public void authenticate()
      throws UnauthorizedException, IllegalArgumentException, NoSuchAlgorithmException, InvalidKeySpecException {

    String token = getTokenFromRequest();
    if (token == null) {
      throw new UnauthorizedException("Unauthorized");
    }

    RSAPublicKey publicKey = getPublicKey();
    if (!verifyHash("store-service", token, publicKey)) {
      throw new UnauthorizedException("Unauthorized");
    }
  }

  private String getTokenFromRequest() {
    // Get api key from request headers
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    return request.getHeader("X-API-KEY");
  }

  private RSAPublicKey getPublicKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
    byte[] publicKeyBytes = Base64.getDecoder().decode(key);
    X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    return (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);
  }

  public boolean verifyHash(String data, String hash, RSAPublicKey publicKey) {
    try {
      Signature signature = Signature.getInstance("SHA256withRSA");
      signature.initVerify(publicKey);
      signature.update(data.getBytes());
      return signature.verify(Base64.getDecoder().decode(hash));
    } catch (IllegalArgumentException | NoSuchAlgorithmException | InvalidKeyException |
             SignatureException e) {
      throw new UnauthorizedException("Unauthorized");
    }

  }
}
