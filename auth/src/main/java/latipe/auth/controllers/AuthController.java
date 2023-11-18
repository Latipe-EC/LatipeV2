package latipe.auth.controllers;


import static latipe.auth.utils.Constants.ErrorCode.TOKEN_EXPIRED;
import static latipe.auth.utils.GenTokenInternal.generateHash;
import static latipe.auth.utils.GenTokenInternal.getPrivateKey;

import feign.Feign;
import feign.Logger;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.okhttp.OkHttpClient;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import latipe.auth.config.ApiPrefixController;
import latipe.auth.config.JwtTokenService;
import latipe.auth.config.SecureInternalProperties;
import latipe.auth.constants.CONSTANTS.TOKEN_TYPE;
import latipe.auth.entity.Role;
import latipe.auth.entity.User;
import latipe.auth.exceptions.BadRequestException;
import latipe.auth.exceptions.NotFoundException;
import latipe.auth.exceptions.UnauthorizedException;
import latipe.auth.feign.TokenClient;
import latipe.auth.repositories.IUserRepository;
import latipe.auth.request.LoginRequest;
import latipe.auth.request.RefreshTokenRequest;
import latipe.auth.request.ResetPasswordByPhoneRequest;
import latipe.auth.request.TokenRequest;
import latipe.auth.request.VerifyAccountRequest;
import latipe.auth.response.LoginResponse;
import latipe.auth.response.RefreshTokenResponse;
import latipe.auth.response.TokenResetPasswordResponse;
import latipe.auth.response.UserCredentialResponse;
import latipe.auth.utils.GenTokenUtils;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@ApiPrefixController("/auth")
@Tag(name = "User authentication")
@Validated
@RequiredArgsConstructor
public class AuthController {

  private final JwtTokenService jwtUtil;
  private final IUserRepository userRepository;
  private final MongoTemplate mongoTemplate;
  private final SecureInternalProperties secureInternalProperties;

  @Value("${URL_FE}")
  private String URL;


  @PostMapping("/login")
  @ResponseStatus(HttpStatus.OK)
  public CompletableFuture<LoginResponse> createAuthenticationToken(
      @RequestBody @Valid LoginRequest loginRequest) {
    return CompletableFuture.supplyAsync(() -> {
      User user = getUser(loginRequest.username());
      if (!jwtUtil.comparePassword(loginRequest.password(), user.getPassword())) {
        throw new BadRequestException("Password not correct");
      }

      if (user.getIsDeleted()) {
        throw new UnauthorizedException("Your account has been deleted");
      }

      if (user.getVerifiedAt() == null) {
        throw new UnauthorizedException("Your account has not been verified");
      }

      final String accessToken = jwtUtil.createAccessToken(user);
      final String refreshToken = jwtUtil.createRefreshToken(user.getEmail());

      return LoginResponse.builder().accessToken(accessToken).refreshToken(refreshToken)
          .id(user.getId()).firstName(user.getFirstName()).lastName(user.getLastName())
          .displayName(user.getDisplayName()).phone(user.getPhoneNumber()).email(user.getEmail())
          .bio(user.getBio()).role(user.getRole().getName()).lastActiveAt(user.getLastLogin())
          .build();
    });
  }

  @PostMapping("/refresh-token")
  @ResponseStatus(HttpStatus.OK)
  public CompletableFuture<RefreshTokenResponse> refreshAuthenticationToken(
      @RequestBody @Valid RefreshTokenRequest refreshTokenRequest) {
    return CompletableFuture.supplyAsync(() -> {
      String refreshToken = refreshTokenRequest.refreshToken();
      // Check if the refresh token is valid and not expired
      String username = jwtUtil.checkToken(refreshToken, "refresh-token");
      User user = getUser(username);
      if (user.getIsDeleted()) {
        throw new UnauthorizedException("Your account has been deleted");
      }

      try {
        if (jwtUtil.validateToken(refreshToken, user)) {
          final String accessToken = jwtUtil.createAccessToken(user);
          refreshToken = jwtUtil.createRefreshToken(user.getEmail());

          return new RefreshTokenResponse(accessToken, refreshToken);
        }
      } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
        throw new RuntimeException(e);
      }
      throw new BadRequestException("Invalid refresh token");
    });
  }

  @PostMapping("/validate-token")
  @ResponseStatus(HttpStatus.OK)
  public CompletableFuture<UserCredentialResponse> validateToken(
      @Valid @RequestBody TokenRequest accessToken) {

    return CompletableFuture.supplyAsync(() -> {
      String username = jwtUtil.checkToken(accessToken.token(), "access-token");
      User user = getUser(username);

      if (user.getPoint() < -100) {
        throw new UnauthorizedException(
            "Your account has been locked due to too many cancellations");
      }

      if (user.getIsDeleted()) {
        throw new UnauthorizedException("Your account has been deleted");
      }

      try {
        if (jwtUtil.validateToken(accessToken.token(), user)) {
          return UserCredentialResponse.builder().email(user.getEmail())
              .phone(user.getPhoneNumber()).id(user.getId()).role(user.getRole().getName()).build();
        }
        throw new UnauthorizedException(TOKEN_EXPIRED);
      } catch (RuntimeException | NoSuchAlgorithmException | InvalidKeySpecException e) {
        throw new RuntimeException(e);
      }
    });
  }

  @PostMapping("/verify-account")
  public Void verifyAccount(@Valid @RequestBody VerifyAccountRequest request) {
    var tokenClient = Feign.builder().client(new OkHttpClient()).encoder(new GsonEncoder())
        .decoder(new GsonDecoder()).logLevel(Logger.Level.FULL)
        .target(TokenClient.class, "http://localhost:8181/api/v1");
    String hash;
    try {
      hash = generateHash("user-service", getPrivateKey(secureInternalProperties.getPrivateKey()));
      return tokenClient.verifyAccount(hash, new VerifyAccountRequest(request.token()));
    } catch (Exception e) {
      throw new BadRequestException(e.getMessage());
    }
  }

  @PostMapping("/request-reset-password-by-email")
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity<Void> requestResetPasswordByEmail(@RequestParam("email") String userEmail)
      throws UnsupportedEncodingException {
    var user = getUser(userEmail);
    String token = UUID.randomUUID().toString();
    LocalDateTime expiredDate = LocalDateTime.now().plusMinutes(5);
    TokenResetPasswordResponse tokenResetPassword = new TokenResetPasswordResponse(token, userEmail,
        expiredDate, TOKEN_TYPE.RESET_PASSWORD);
    GenTokenUtils.setToken(user, tokenResetPassword);
    userRepository.save(user);
    String resetPasswordUrl =
        URL + URLEncoder.encode(user.getTokenResetPassword(), StandardCharsets.UTF_8);
    // notification service will cover
//        emailService.sendResetPasswordEmail(userEmail, resetPasswordUrl);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/reset-password")
  public ResponseEntity<?> resetPasswordByToken(@RequestBody ResetPasswordByPhoneRequest input) {
    var user = userRepository.findByTokenResetPassword(input.token())
        .orElseThrow(() -> new NotFoundException("User not found"));
    TokenResetPasswordResponse tokenResetPassword = GenTokenUtils.decodeToken(
        user.getTokenResetPassword());
    if (tokenResetPassword == null || GenTokenUtils.isExpired(tokenResetPassword.expired())) {
      return ResponseEntity.badRequest().body("Token is expired");
    }
    if (!tokenResetPassword.email().equals(user.getEmail())) {
      return ResponseEntity.badRequest().body("Email not match!");
    }
    user.setRequestCount(0);
    user.setLastRequest(null);
    user.setHashedPassword(jwtUtil.hashPassword(input.newPassword()));
    userRepository.save(user);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/request-pin/{phone}")
  public void requestPin(@PathVariable("phone") String phoneNumber) {
    User user = userRepository.findByPhoneNumber(phoneNumber)
        .orElseThrow(() -> new NotFoundException("User not found"));
    TokenResetPasswordResponse token = TokenResetPasswordResponse.builder()
        .token(GenTokenUtils.generateRandomDigits()).email(user.getEmail())
        .expired(LocalDateTime.now().plusMinutes(1).plusSeconds(2)).type(TOKEN_TYPE.VERIFY_EMAIL)
        .build();
    GenTokenUtils.setToken(user, token);
    userRepository.save(user);
    String messageText = "Mã xác thực của bạn là: %s".formatted(token.token());
    // notification will cover
//        smsService.sendSMS(phoneNumber.replaceFirst("^0", "+84"), messageText);
  }

  @PostMapping("/validate-pin/{phone}")
  public ResponseEntity<String> validatePin(@PathVariable String phone,
      @RequestBody TokenRequest input) throws UnsupportedEncodingException {
    User user = userRepository.findByPhoneNumber(phone)
        .orElseThrow(() -> new NotFoundException("User not found"));
    TokenResetPasswordResponse tokenResetPassword = GenTokenUtils.decodeToken(
        user.getTokenResetPassword());
    if (tokenResetPassword == null || GenTokenUtils.isExpired(tokenResetPassword.expired())) {
      return ResponseEntity.badRequest().body("Token is expired");
    }
    if (!tokenResetPassword.token().equals(input.token())) {
      return ResponseEntity.badRequest().body("Pin code is not correct!");
    }
    tokenResetPassword.withExpired(LocalDateTime.now().plusMinutes(5));
    user.setTokenResetPassword(GenTokenUtils.encodeToken(tokenResetPassword));
    userRepository.save(user);
    return ResponseEntity.ok(
        URL + URLEncoder.encode(user.getTokenResetPassword(), StandardCharsets.UTF_8));
  }

  private User getUser(String username) {
    Criteria criteria = new Criteria().orOperator(Criteria.where("email").is(username),
        Criteria.where("phoneNumber").is(username));

    Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(criteria),
//        new CustomAggregationOperation(lookup),
        Aggregation.lookup(mongoTemplate.getCollectionName(Role.class), "roleId", "_id", "roles"),
        Aggregation.addFields()
            .addFieldWithValue("role", ArrayOperators.arrayOf("roles").elementAt(0)).build());
    var userRaw = mongoTemplate.aggregate(aggregation, mongoTemplate.getCollectionName(User.class),
        Document.class).getUniqueMappedResult();
    if (userRaw == null) {
      throw new NotFoundException("User not found");
    }
    var user = mongoTemplate.getConverter().read(User.class, userRaw);
    user.setRole(
        mongoTemplate.getConverter().read(Role.class, userRaw.get("role", Document.class)));
    return user;
  }
}
