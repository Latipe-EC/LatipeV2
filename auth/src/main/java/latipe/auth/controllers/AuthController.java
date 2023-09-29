package latipe.auth.controllers;


import static latipe.auth.utils.Constants.ErrorCode.TOKEN_EXPIRED;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import latipe.auth.Entity.User;
import latipe.auth.config.ApiPrefixController;
import latipe.auth.config.JwtTokenService;
import latipe.auth.constants.CONSTANTS.TOKEN_TYPE;
import latipe.auth.exceptions.BadRequestException;
import latipe.auth.exceptions.NotFoundException;
import latipe.auth.exceptions.UnauthorizedException;
import latipe.auth.repositories.IUserRepository;
import latipe.auth.request.LoginRequest;
import latipe.auth.request.RefreshTokenRequest;
import latipe.auth.request.ResetPasswordByPhoneRequest;
import latipe.auth.request.TokenRequest;
import latipe.auth.response.LoginResponse;
import latipe.auth.response.RefreshTokenResponse;
import latipe.auth.response.TokenResetPasswordResponse;
import latipe.auth.response.UserCredentialResponse;
import latipe.auth.utils.GenTokenUtils;
import org.springframework.beans.factory.annotation.Value;
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
public class AuthController {

  private final JwtTokenService jwtUtil;
  //    private final IUserRepository userRepository;
  private final IUserRepository userRepository;

  @Value("${URL_FE}")
  private String URL;

  public AuthController(JwtTokenService jwtUtil, IUserRepository userRepository) {
    this.jwtUtil = jwtUtil;
    this.userRepository = userRepository;
  }

  @PostMapping("/login")
  @ResponseStatus(HttpStatus.OK)
  public CompletableFuture<LoginResponse> createAuthenticationToken(
      @RequestBody @Valid LoginRequest loginRequest) {
    return CompletableFuture.supplyAsync(() -> {
      User user = getUser(loginRequest.username());
      if (!jwtUtil.comparePassword(loginRequest.password(), user.getPassword())) {
        throw new BadRequestException("Password not correct");
      }
      final String accessToken = jwtUtil.generateAccessToken(user);
      final String refreshToken = jwtUtil.generateRefreshToken(user);
      return LoginResponse.builder()
          .accessToken(accessToken)
          .refreshToken(refreshToken)
          .id(user.getId())
          .firstName(user.getFirstName())
          .lastName(user.getLastName())
          .displayName(user.getDisplayName())
          .phone(user.getPhoneNumber())
          .email(user.getEmail())
          .bio(user.getBio())
          .role(user.getRole().getName())
          .lastActiveAt(user.getLastLogin())
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
      try {
        if (jwtUtil.validateToken(refreshToken, user)) {
          final String accessToken = jwtUtil.generateAccessToken(user);
          refreshToken= jwtUtil.generateRefreshToken(user);

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
          return UserCredentialResponse.builder()
              .email(user.getEmail())
              .phone(user.getPhoneNumber())
              .id(user.getId())
              .role(user.getRole().getName())
              .build();
        }
        throw new UnauthorizedException(TOKEN_EXPIRED);
      } catch (RuntimeException | NoSuchAlgorithmException | InvalidKeySpecException e) {
        throw new RuntimeException(e);
      }
    });
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
        .token(GenTokenUtils.generateRandomDigits())
        .email(user.getEmail())
        .expired(LocalDateTime.now().plusMinutes(1).plusSeconds(2))
        .type(TOKEN_TYPE.VERIFY_EMAIL)
        .build();
    GenTokenUtils.setToken(user, token);
    user = userRepository.save(user);
    String messageText = "Mã xác thực của bạn là: %s".formatted(token.token());
    // notification will cover
//        smsService.sendSMS(phoneNumber.replaceFirst("^0", "+84"), messageText);
  }

  @PostMapping("/validate-pin/{phone}")
  public ResponseEntity<String> validatePin(@PathVariable String phone,
      @RequestBody TokenRequest input)
      throws UnsupportedEncodingException {
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
    List<User> users = userRepository.findByPhoneAndEmail(username);
    if (users.isEmpty()) {
      throw new NotFoundException("Cannot find user with email");
    }
    return users.get(0);
  }
}