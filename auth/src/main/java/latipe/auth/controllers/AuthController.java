package latipe.auth.controllers;


import static latipe.auth.utils.Constants.ErrorCode.TOKEN_EXPIRED;
import static latipe.auth.utils.GenTokenInternal.generateHash;
import static latipe.auth.utils.GenTokenInternal.getPrivateKey;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Feign;
import feign.Logger;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.okhttp.OkHttpClient;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.CompletableFuture;
import latipe.auth.config.ApiPrefixController;
import latipe.auth.config.JwtTokenService;
import latipe.auth.config.SecureInternalProperties;
import latipe.auth.entity.Role;
import latipe.auth.entity.User;
import latipe.auth.exceptions.BadRequestException;
import latipe.auth.exceptions.NotFoundException;
import latipe.auth.exceptions.UnauthorizedException;
import latipe.auth.feign.TokenClient;
import latipe.auth.feign.UserClient;
import latipe.auth.request.ForgotPasswordRequest;
import latipe.auth.request.LoginRequest;
import latipe.auth.request.RefreshTokenRequest;
import latipe.auth.request.RegisterRequest;
import latipe.auth.request.RequestVerifyAccountRequest;
import latipe.auth.request.ResetPasswordRequest;
import latipe.auth.request.TokenRequest;
import latipe.auth.request.VerifyAccountRequest;
import latipe.auth.response.LoginResponse;
import latipe.auth.response.RefreshTokenResponse;
import latipe.auth.response.UserCredentialResponse;
import latipe.auth.response.UserResponse;
import latipe.auth.viewmodel.ErrorMessage;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@ApiPrefixController("/auth")
@Tag(name = "User authentication")
@Validated
@RequiredArgsConstructor
public class AuthController {

  private final JwtTokenService jwtUtil;
  private final MongoTemplate mongoTemplate;
  private final SecureInternalProperties secureInternalProperties;
  private final ObjectMapper objectMapper;

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

      if (user.getVerifiedAt() == null) {
        throw new UnauthorizedException("Your account has not been verified");
      }

      if (user.getIsBan()) {
        throw new UnauthorizedException("Your account has been banned");
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
  public Void verifyAccount(@Valid @RequestBody RequestVerifyAccountRequest request) {
    var tokenClient = Feign.builder().client(new OkHttpClient()).encoder(new GsonEncoder())
        .decoder(new GsonDecoder()).logLevel(Logger.Level.FULL)
        .target(TokenClient.class, "http://localhost:8181/api/v1");
    String hash;
    try {
      hash = generateHash("user-service", getPrivateKey(secureInternalProperties.getPrivateKey()));
      tokenClient.requestVerifyAccount(hash, request);
      return null;
    } catch (Exception e) {
      return renderErrorMsg(e);
    }
  }

  @PostMapping("/finish-verify-account")
  public Void requestVerifyAccount(@Valid @RequestBody VerifyAccountRequest request) {
    var tokenClient = Feign.builder().client(new OkHttpClient()).encoder(new GsonEncoder())
        .decoder(new GsonDecoder()).logLevel(Logger.Level.FULL)
        .target(TokenClient.class, "http://localhost:8181/api/v1");
    String hash;
    try {
      hash = generateHash("user-service", getPrivateKey(secureInternalProperties.getPrivateKey()));
      return tokenClient.verifyAccount(hash, request);
    } catch (Exception e) {
      return renderErrorMsg(e);
    }
  }

  @PostMapping("/forgot-password")
  public Void verifyAccount(@Valid @RequestBody ForgotPasswordRequest request) {
    var tokenClient = Feign.builder().client(new OkHttpClient()).encoder(new GsonEncoder())
        .decoder(new GsonDecoder()).logLevel(Logger.Level.FULL)
        .target(TokenClient.class, "http://localhost:8181/api/v1");
    String hash;
    try {
      hash = generateHash("user-service", getPrivateKey(secureInternalProperties.getPrivateKey()));
      tokenClient.forgotPassword(hash, request);
      return null;
    } catch (Exception e) {
      return renderErrorMsg(e);
    }
  }

  @PostMapping("/reset-password")
  public Void verifyAccount(@Valid @RequestBody ResetPasswordRequest request) {
    var tokenClient = Feign.builder().client(new OkHttpClient()).encoder(new GsonEncoder())
        .decoder(new GsonDecoder()).logLevel(Logger.Level.FULL)
        .target(TokenClient.class, "http://localhost:8181/api/v1");
    String hash;
    try {
      hash = generateHash("user-service", getPrivateKey(secureInternalProperties.getPrivateKey()));
      tokenClient.resetPassword(hash, request);
      return null;
    } catch (Exception e) {
      return renderErrorMsg(e);
    }
  }

  @PostMapping("/register")
  public UserResponse verifyAccount(@Valid @RequestBody RegisterRequest request) {
    var userClient = Feign.builder().client(new OkHttpClient())
        .encoder(new GsonEncoder())
        .decoder(new GsonDecoder()).logLevel(Logger.Level.FULL)
        .target(UserClient.class, "http://localhost:8181/api/v1");
    String hash;
    try {
      hash = generateHash("user-service", getPrivateKey(secureInternalProperties.getPrivateKey()));
      return userClient.register(hash, request);
    } catch (Exception e) {
      renderErrorMsg(e);
      return null;
    }
  }

  private Void renderErrorMsg(Exception e) {
    int startIndex = e.getMessage().indexOf("{");
    int endIndex = e.getMessage().lastIndexOf("}") + 1;
    String json = e.getMessage().substring(startIndex, endIndex);
    ErrorMessage error;
    try {
      error = objectMapper.readValue(json, ErrorMessage.class);
    } catch (JsonProcessingException ex) {
      throw new RuntimeException(ex);
    }

    if (error.getStatusCode() == null) {
      throw new RuntimeException("Internal server error");
    } else if (error.getStatusCode().contains("404")) {
      throw new NotFoundException(error.getDetail());
    } else if (error.getStatusCode().contains("401")) {
      throw new UnauthorizedException(error.getDetail());
    } else {
      throw new BadRequestException(error.getDetail());
    }
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
