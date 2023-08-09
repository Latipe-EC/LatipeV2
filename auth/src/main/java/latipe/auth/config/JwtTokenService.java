package latipe.auth.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtTokenService {
    private static final int COST = 12;

    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.public-key}")
    private static String public_key;
    @Value("${jwt.private-key}")
    private String private_key;
    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;
    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken("access-token", claims, userDetails.getUsername(), accessTokenExpiration);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken("refresh-token", claims, userDetails.getUsername(), refreshTokenExpiration);
    }

    public static String getUsernameFromToken(String token) throws NoSuchAlgorithmException, InvalidKeySpecException {
        RSAPublicKey publicKey = getPublicKey();
        Jws<Claims> jws = Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token);
        return jws.getBody().getSubject();
    }

    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(COST));
    }

    public static boolean comparePassword(String password, String storedHash) {
        return BCrypt.checkpw(password, storedHash);
    }

    public static Date getExpirationDateFromToken(String token) throws NoSuchAlgorithmException, InvalidKeySpecException {
        RSAPublicKey publicKey = getPublicKey();
        Jws<Claims> jws = Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token);
        return jws.getBody().getExpiration();

    }

    public static boolean validateToken(String token, UserDetails userDetails) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public static boolean isTokenExpired(String token) throws NoSuchAlgorithmException, InvalidKeySpecException {
        Date expirationDate = getExpirationDateFromToken(token);
        return expirationDate.before(new Date());
    }

    public String checkRefreshToken(String token) throws NoSuchAlgorithmException, InvalidKeySpecException {
        RSAPublicKey publicKey = getPublicKey();
        Jws<Claims> jws = Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token);
        if (!jws.getBody().get("type", String.class).equals("refresh-token") )
            return null;
        return jws.getBody().getSubject();
    }

    private Claims getClaimsFromToken(String token) {
        try {
            RSAPublicKey publicKey = getPublicKey();
            return Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token).getBody();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error generating token", e);
        }
    }

    private String createToken(String type, Map<String, Object> claims, String subject, long expiration) {
        try {
            RSAPrivateKey privateKey = getPrivateKey();
            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(subject)
                    .claim("type", type)
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + expiration))
                    .signWith(privateKey, SignatureAlgorithm.RS512)
                    .compact();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error generating token", e);
        }
    }

    private RSAPrivateKey getPrivateKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] privateKeyBytes = Base64.getDecoder().decode(private_key.replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", ""));
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) keyFactory.generatePrivate(privateKeySpec);
    }

    private static RSAPublicKey getPublicKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] publicKeyBytes = Base64.getDecoder().decode(public_key.replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", ""));
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);
    }
}
