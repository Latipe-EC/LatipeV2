package latipe.product.configs;

import static java.lang.System.out;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;

public class test {

  @Value("${secure-internal.public-key}")
  private String key;
  @Value("${secure-internal.private-key}")
  private String privateKey;

  public static void main(String[] args) throws Exception {
    generateHash("test", getPrivateKey());
    var hash = generateHash("product-service", getPrivateKey());
    out.println("Public key: " + hash);
    out.println("Public key: " + verifyHash("product-service", hash, getPublicKey()));
  }

  private static RSAPublicKey getPublicKey()
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    byte[] publicKeyBytes = Base64.getDecoder().decode(
        "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAKl5SroD9NuxQ955qCcCMERMAMJz2/CZfx3WTHkQa8cvf13FBkRFvRkLD7U1MRCNylxe+oebmy/OF4GzuSxcDl8CAwEAAQ==");
    X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    return (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);
  }

  public static RSAPrivateKey getPrivateKey()
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    byte[] privateKeyBytes = Base64.getDecoder().decode(
        "MIIBUwIBADANBgkqhkiG9w0BAQEFAASCAT0wggE5AgEAAkEAqXlKugP027FD3nmoJwIwREwAwnPb8Jl/HdZMeRBrxy9/XcUGREW9GQsPtTUxEI3KXF76h5ubL84XgbO5LFwOXwIDAQABAkB7y8dDZFp8FNwv6oxjmlyptx8i7EEwWuAZao5ILS+duqnmZLLdSVpTF/tc58bFoBsZyWFru6sxmommxDipxwMBAiEA5FhOxbJeo8zn4OyASEzBuzWNv2QpEX7hpn5H9wlTVoECIQC9/7iLKBzB7YiS6+tKHRROHQt9n4OMcDGau5gfxXG03wIgRmGRpg3cbdByiDldMOu3quRO1Hci0WmyU4cI13PgZAECIEMMNbxtqFBLGXH3bO2Xe23hVAe9vbdWdDrNTm6Px4NzAiBjaQjKKLaSlP24wdaYe0cQxWuC0UlwAu5vHk0twRzV/g==");
    PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    return (RSAPrivateKey) keyFactory.generatePrivate(privateKeySpec);
  }

  public static String generateHash(String data, RSAPrivateKey privateKey) throws Exception {
    Signature signature = Signature.getInstance("SHA256withRSA");
    signature.initSign(privateKey);
    signature.update(data.getBytes());
    return Base64.getEncoder().encodeToString(signature.sign());
  }

  public static boolean verifyHash(String data, String hash, RSAPublicKey publicKey)
      throws Exception {
    Signature signature = Signature.getInstance("SHA256withRSA");
    signature.initVerify(publicKey);
    signature.update(data.getBytes());
    return signature.verify(Base64.getDecoder().decode(hash));
  }
}
