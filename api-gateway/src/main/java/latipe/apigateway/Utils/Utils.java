package latipe.apigateway.Utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Enumeration;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import latipe.apigateway.constants.Const;

public class Utils {

    public static boolean validSid(String sidEncoded) {
        try {
            byte[] decryptedBytes = decryptAES(Base64.getDecoder().decode(sidEncoded));
            String jsonString = new String(decryptedBytes, StandardCharsets.UTF_8);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Error decoding token", e);
        }
    }

    private static byte[] decryptAES(byte[] encryptedData) throws Exception {
        Key key = new SecretKeySpec(Const.ENCRYPTION_KEY.getBytes(StandardCharsets.UTF_8),
            Const.AES_ALGORITHM);
        Cipher cipher = Cipher.getInstance(Const.AES_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(encryptedData);
    }

    private static byte[] encryptAES(byte[] data) throws Exception {
        Key key = new SecretKeySpec(Const.ENCRYPTION_KEY.getBytes(StandardCharsets.UTF_8),
            Const.AES_ALGORITHM);
        Cipher cipher = Cipher.getInstance(Const.AES_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    public static String encodeSession(String ip) {
        try {
            String jsonString = new ObjectMapper().writeValueAsString(
                ip + " " + LocalDateTime.now());
            byte[] encryptedBytes = encryptAES(jsonString.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error encoding session", e);
        }
    }

    public static String getRealIp(HttpServletRequest request) {
        var ipAddress = request.getRemoteAddr();

        Enumeration<String> xForwardedFor = request.getHeaders("X-Forwarded-For");

        if (xForwardedFor.hasMoreElements()) {
            ipAddress = xForwardedFor.nextElement(); // The real IP address is usually the first one in the list
        }

        return ipAddress;
    }
}
