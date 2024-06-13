package latipe.auth.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import latipe.auth.entity.User;
import latipe.auth.exceptions.BadRequestException;
import latipe.auth.response.TokenResetPasswordResponse;

public class GenTokenUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(
        new JavaTimeModule());
    private static final String ENCRYPTION_KEY = "Jf57xtfgC5X9tktm"; // Thay đổi bằng khóa bí mật 16 ký tự của bạn
    private static final String AES_ALGORITHM = "AES";

    public static String generateRandomDigits() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    public static String encodeToken(TokenResetPasswordResponse tokenResetPassword) {
        try {
            String jsonString = objectMapper.writeValueAsString(tokenResetPassword);
            byte[] encryptedBytes = encryptAES(jsonString.getBytes(StandardCharsets.UTF_8),
                ENCRYPTION_KEY);
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error encoding token", e);
        }
    }

    public static TokenResetPasswordResponse decodeToken(String encodedToken) {
        try {
            byte[] decryptedBytes = decryptAES(Base64.getDecoder().decode(encodedToken),
                ENCRYPTION_KEY);
            String jsonString = new String(decryptedBytes, StandardCharsets.UTF_8);
            return objectMapper.readValue(jsonString, TokenResetPasswordResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Error decoding token", e);
        }
    }

    private static byte[] encryptAES(byte[] data, String encryptKey) throws Exception {
        Key key = new SecretKeySpec(encryptKey.getBytes(StandardCharsets.UTF_8), AES_ALGORITHM);
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    private static byte[] decryptAES(byte[] encryptedData, String encryptKey) throws Exception {
        Key key = new SecretKeySpec(encryptKey.getBytes(StandardCharsets.UTF_8), AES_ALGORITHM);
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(encryptedData);
    }

    public static void setToken(User user, TokenResetPasswordResponse token) {
        user.setTokenResetPassword(encodeToken(token));
        if (user.getRequestCount() > 3
            && (user.getLastRequest() == null || user.getLastRequest().getTime() -
            new Date().getTime()
            < 24 * 60 * 60 * 1000)) {
            throw new BadRequestException(
                "Bạn đã gửi quá nhiều yêu cầu để cấp lại mật khẩu, hãy đợi 24h sau để thực hiện lại chức năng");
        }
        user.setRequestCount(user.getRequestCount() + 1);
        user.setLastRequest(new Date());
    }

    public static boolean isExpired(LocalDateTime expired) {
        return LocalDateTime.now().isAfter(expired);
    }

    public static String encodeSession(String id, String encryptKey) {
        try {
            String jsonString = objectMapper.writeValueAsString(genSid(id));
            byte[] encryptedBytes = encryptAES(jsonString.getBytes(StandardCharsets.UTF_8),
                encryptKey);
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error encoding session", e);
        }
    }

    private static String genSid(String email) {
        return email + "-" + System.currentTimeMillis();
    }
}
