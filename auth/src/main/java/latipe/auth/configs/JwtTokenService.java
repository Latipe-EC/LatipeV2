package latipe.auth.configs;

import static latipe.auth.utils.Constants.ErrorCode.TOKEN_EXPIRED;
import static latipe.auth.utils.Constants.ErrorCode.TOKEN_INVALID;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import latipe.auth.exceptions.BadRequestException;
import latipe.auth.exceptions.UnauthorizedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

  private static final int COST = 12;
  @Value("${jwt.public-key}")
  private String public_key;
  @Value("${jwt.private-key}")
  private String private_key;
  @Value("${jwt.access-token-expiration}")
  private long accessTokenExpiration;
  @Value("${jwt.refresh-token-expiration}")
  private long refreshTokenExpiration;

  public String getUsernameFromToken(String token)
      throws NoSuchAlgorithmException, InvalidKeySpecException, RuntimeException {
    RSAPublicKey publicKey = getPublicKey();
    Jws<Claims> jws = Jwts.parserBuilder()
        .setSigningKey(publicKey)
        .build()
        .parseClaimsJws(token);
    return jws.getBody().getSubject();
  }

  public Claims getBodyToken(String token)
      throws NoSuchAlgorithmException, InvalidKeySpecException, RuntimeException {
    RSAPublicKey publicKey = getPublicKey();
    Jws<Claims> jws = Jwts.parserBuilder()
        .setSigningKey(publicKey)
        .build()
        .parseClaimsJws(token);
    return jws.getBody();
  }

  public String hashPassword(String password) {
    return BCrypt.hashpw(password, BCrypt.gensalt(COST));
  }

  public boolean comparePassword(String password, String storedHash) {
    return BCrypt.checkpw(password, storedHash);
  }

  public Date getExpirationDateFromToken(String token)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    RSAPublicKey publicKey = getPublicKey();
    Jws<Claims> jws = Jwts.parserBuilder()
        .setSigningKey(publicKey)
        .build()
        .parseClaimsJws(token);
    return jws.getBody().getExpiration();

  }

  public boolean validateToken(String token, UserDetails userDetails)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    String username = getUsernameFromToken(token);
    return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
  }

  public boolean isTokenExpired(String token) {
    Date expirationDate;
    try {
      expirationDate = getExpirationDateFromToken(token);
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      return false;
    }
    return expirationDate.before(new Date());
  }

  public String createAccessToken(UserDetails user) {
    try {
      RSAPrivateKey privateKey = getPrivateKey();
      return Jwts.builder()
          .setSubject(user.getUsername())
          .claim("type", "access-token")
          .claim("role", user.getAuthorities())
          .setIssuedAt(new Date(System.currentTimeMillis()))
          .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
          .signWith(privateKey, SignatureAlgorithm.RS512)
          .compact();
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new RuntimeException("Error generating token", e);
    }
  }

  public String createRefreshToken(String subject) {
    try {
      RSAPrivateKey privateKey = getPrivateKey();
      return Jwts.builder()
          .setSubject(subject)
          .claim("type", "refresh-token")
          .setIssuedAt(new Date(System.currentTimeMillis()))
          .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
          .signWith(privateKey, SignatureAlgorithm.RS512)
          .compact();
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new RuntimeException("Error generating token", e);
    }
  }

  private RSAPrivateKey getPrivateKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
    byte[] privateKeyBytes = Base64.getDecoder().decode(
        private_key.replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s+", ""));
    PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    return (RSAPrivateKey) keyFactory.generatePrivate(privateKeySpec);
  }

  private RSAPublicKey getPublicKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
    byte[] publicKeyBytes = Base64.getDecoder().decode(
        public_key.replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "")
            .replaceAll("\\s+", ""));
    X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    return (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);
  }

  public String checkToken(String token, String type) {
    String username;
    Claims claims;
    try {
      claims = getBodyToken(token);
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new UnauthorizedException(TOKEN_INVALID);
    } catch (ExpiredJwtException e) {
      throw new UnauthorizedException(TOKEN_EXPIRED);
    }
    if (claims.get("type", String.class).equals(type)) {
      username = claims.getSubject();
    } else {
      throw new UnauthorizedException(TOKEN_INVALID);
    }
    if (username == null) {
      throw new BadRequestException("Not Type %s".formatted(type).replace("-", " "));
    }
    return username;
  }
}
