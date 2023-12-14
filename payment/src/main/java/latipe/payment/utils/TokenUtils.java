package latipe.payment.utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import latipe.payment.exceptions.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriComponentsBuilder;

public class TokenUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(TokenUtils.class);

  public static String encodeToken(String id, String ENCRYPTION_KEY) {
    try {
      byte[] encryptedBytes = encryptAES(id.getBytes(StandardCharsets.UTF_8), ENCRYPTION_KEY);
      var encodedToken = Base64.getEncoder().encodeToString(encryptedBytes);
      LOGGER.info("encryptedBytes: " + encodedToken);
      return  URLEncoder.encode(encodedToken, StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new BadRequestException("Error encoding token", e);
    }
  }

  public static String decodeToken(String encodedToken, String ENCRYPTION_KEY) {
    try {

      byte[] decryptedBytes = decryptAES(Base64.getDecoder().decode(encodedToken), ENCRYPTION_KEY);
      return new String(decryptedBytes, StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new BadRequestException("Error decoding token", e);
    }
  }

  private static byte[] encryptAES(byte[] data, String ENCRYPTION_KEY) throws Exception {
    Key key = new SecretKeySpec(ENCRYPTION_KEY.getBytes(StandardCharsets.UTF_8), "AES");
    Cipher cipher = Cipher.getInstance("AES");
    cipher.init(Cipher.ENCRYPT_MODE, key);
    return cipher.doFinal(data);
  }

  private static byte[] decryptAES(byte[] encryptedData, String ENCRYPTION_KEY) throws Exception {
    Key key = new SecretKeySpec(ENCRYPTION_KEY.getBytes(StandardCharsets.UTF_8), "AES");
    Cipher cipher = Cipher.getInstance("AES");
    cipher.init(Cipher.DECRYPT_MODE, key);
    return cipher.doFinal(encryptedData);
  }
}
