package latipe.auth.controllers;


import static latipe.auth.utils.AuthenticationUtils.genKeyCacheToken;
import static latipe.auth.utils.AuthenticationUtils.getMethodName;
import static latipe.auth.utils.Constants.ErrorCode.TOKEN_EXPIRED;
import static latipe.auth.utils.GenTokenInternal.generateHash;
import static latipe.auth.utils.GenTokenInternal.getPrivateKey;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.okhttp.OkHttpClient;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.CompletableFuture;
import latipe.auth.annotations.ApiPrefixController;
import latipe.auth.configs.JwtTokenService;
import latipe.auth.configs.SecureInternalProperties;
import latipe.auth.entity.Role;
import latipe.auth.entity.User;
import latipe.auth.exceptions.BadRequestException;
import latipe.auth.exceptions.ForbiddenException;
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
import latipe.auth.services.TokenCache;
import latipe.auth.utils.GetInstanceServer;
import latipe.auth.viewmodel.ErrorMessage;
import latipe.auth.viewmodel.LogMessage;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@ApiPrefixController("/auth")
@Tag(name = "User authentication")
@Validated
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

  private final JwtTokenService jwtUtil;
  private final MongoTemplate mongoTemplate;
  private final SecureInternalProperties secureInternalProperties;
  private final ObjectMapper objectMapper;
  private final TokenCache tokenCache;
  private final Gson gson;

  private final LoadBalancerClient loadBalancer;
  private final GsonDecoder gsonDecoder;
  private final GsonEncoder gsonEncoder;
  private final OkHttpClient okHttpClient;

  @Value("${service.user}")
  private String userService;

  @Value("${eureka.client.enabled}")
  private boolean useEureka;

  @PostMapping("/login")
  @ResponseStatus(HttpStatus.OK)
  public CompletableFuture<LoginResponse> createAuthenticationToken(
      @RequestBody @Valid LoginRequest loginRequest, HttpServletRequest request) {
    return CompletableFuture.supplyAsync(() -> {
      LOGGER.info(gson.toJson(
          LogMessage.create("Login request from user %s".formatted(loginRequest.username()),
              request, getMethodName())));

      var user = getUser(loginRequest.username());
      if (!jwtUtil.comparePassword(loginRequest.password(), user.getPassword())) {
        throw new BadRequestException("Password not correct");
      }

      if (user.getIsBanned()) {
        throw new UnauthorizedException("Your account has been banned");
      }

      if (user.getIsDeleted()) {
        throw new UnauthorizedException("Your account has been deleted");
      }

      if (user.getVerifiedAt() == null) {
        throw new UnauthorizedException("Your account has not been verified");
      }

      final String accessToken = jwtUtil.createAccessToken(user);
      final String refreshToken = jwtUtil.createRefreshToken(user.getEmail());

      tokenCache.cacheToken(genKeyCacheToken(accessToken, user.getEmail()),
          gson.toJson(UserCredentialResponse.builder()
              .email(user.getEmail())
              .phone(user.getPhoneNumber())
              .id(user.getId())
              .role(user.getRole().getName())
              .build()));

      LOGGER.info("Login success for user {}", loginRequest.username());
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
      @RequestBody @Valid RefreshTokenRequest refreshTokenRequest, HttpServletRequest request) {
    LOGGER.info(
        gson.toJson(LogMessage.create("Refresh token request", request, getMethodName())));

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
          tokenCache.cacheToken(genKeyCacheToken(accessToken, user.getEmail()),
              gson.toJson(UserCredentialResponse.builder()
                  .email(user.getEmail())
                  .phone(user.getPhoneNumber())
                  .id(user.getId())
                  .role(user.getRole().getName())
                  .build()));

          LOGGER.info("Refresh token success for user {}", username);
          return new RefreshTokenResponse(accessToken, refreshToken);
        }
      } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
        throw new RuntimeException(e);
      }
      throw new BadRequestException("Invalid refresh token");
    });
  }

  @PostMapping("/validate-token")
  public CompletableFuture<UserCredentialResponse> validateToken(
      @Valid @RequestBody TokenRequest accessToken, HttpServletRequest request) {

    LOGGER.info(
        gson.toJson(LogMessage.create("Validate token request", request, getMethodName())));
    String username = jwtUtil.checkToken(accessToken.token(), "access-token");

    var userCache = tokenCache.getUserDetails(genKeyCacheToken(accessToken.token(), username));

    if (userCache != null) {
      LOGGER.info("Validate token success for user {}", username);
      var user = gson.fromJson(userCache, UserCredentialResponse.class);
      return CompletableFuture.completedFuture(
          UserCredentialResponse.builder().email(user.email())
              .phone(user.phone()).id(user.id()).role(user.role()).build());
    }

    return CompletableFuture.supplyAsync(() -> {
      var user = getUser(username);

      if (user.getPoint() < -100) {
        throw new BadRequestException(
            "Your account has been locked due to too many cancellations");
      }

      if (user.getIsDeleted()) {
        throw new BadRequestException("Your account has been deleted");
      }

      if (user.getVerifiedAt() == null) {
        throw new BadRequestException("Your account has not been verified");
      }

      if (user.getIsBanned()) {
        throw new ForbiddenException("Your account has been banned");
      }

      try {
        if (jwtUtil.validateToken(accessToken.token(), user)) {
          LOGGER.info("Validate token success for user {}", username);
          return UserCredentialResponse.builder().email(user.getEmail())
              .phone(user.getPhoneNumber()).id(user.getId()).role(user.getRole().getName()).build();
        }
        throw new UnauthorizedException(TOKEN_EXPIRED);
      } catch (RuntimeException | NoSuchAlgorithmException | InvalidKeySpecException e) {
        LOGGER.error("Error validating token: {}", e.getMessage());
        throw new RuntimeException(e);
      }
    });
  }

  @PostMapping("/verify-account")
  public Void verifyAccount(@Valid @RequestBody RequestVerifyAccountRequest input,
      HttpServletRequest request) {

    LOGGER.info(gson.toJson(
        LogMessage.create("Verify account request from user %s".formatted(input.email()),
            request, getMethodName())));
    String hash;
    try {
      hash = generateHash("user-service", getPrivateKey(secureInternalProperties.getPrivateKey()));
      var tokenClient =
          Feign.builder().client(okHttpClient).encoder(gsonEncoder)
              .decoder(gsonDecoder).target(TokenClient.class,
                  useEureka ? String.format("%s/api/v1", GetInstanceServer.get(
                      loadBalancer, userService
                  )) : userService);
      tokenClient.requestVerifyAccount(hash, input);
      LOGGER.info("Verify account success for user {}", input.email());
      return null;
    } catch (Exception e) {
      LOGGER.error("Error verifying account {}: {}", input.email(), e.getMessage());
      return renderErrorMsg(e);
    }
  }

  @PostMapping("/finish-verify-account")
  public Void requestVerifyAccount(@Valid @RequestBody VerifyAccountRequest input,
      HttpServletRequest request) {

    LOGGER.info(
        gson.toJson(LogMessage.create("Finish verify account request", request, getMethodName())
        ));

    String hash;
    try {
      hash = generateHash("user-service", getPrivateKey(secureInternalProperties.getPrivateKey()));
      LOGGER.info("Finish verify account success");
      var tokenClient =
          Feign.builder().client(okHttpClient).encoder(gsonEncoder)
              .decoder(gsonDecoder).target(TokenClient.class,
                  useEureka ? String.format("%s/api/v1", GetInstanceServer.get(
                      loadBalancer, userService
                  )) : userService);

      return tokenClient.verifyAccount(hash, input);
    } catch (Exception e) {
      LOGGER.error("Error finishing verify account: {}", e.getMessage());
      return renderErrorMsg(e);
    }
  }

  @PostMapping("/forgot-password")
  public Void verifyAccount(@Valid @RequestBody ForgotPasswordRequest input,
      HttpServletRequest request) {

    LOGGER.info(gson.toJson(
        LogMessage.create("Forgot password request from user %s".formatted(input.email()), request,
            getMethodName())
    ));

    String hash;
    try {
      hash = generateHash("user-service", getPrivateKey(secureInternalProperties.getPrivateKey()));
      var tokenClient =
          Feign.builder().client(okHttpClient).encoder(gsonEncoder)
              .decoder(gsonDecoder).target(TokenClient.class,
                  useEureka ? String.format("%s/api/v1", GetInstanceServer.get(
                      loadBalancer, userService
                  )) : userService);

      tokenClient.forgotPassword(hash, input);
      LOGGER.info("Forgot password success for user {}", input.email());
      return null;
    } catch (Exception e) {
      LOGGER.error("Error forgot password: {}", e.getMessage());
      return renderErrorMsg(e);
    }
  }

  @PostMapping("/reset-password")
  public Void verifyAccount(@Valid @RequestBody ResetPasswordRequest input,
      HttpServletRequest request) {

    LOGGER.info(
        gson.toJson(LogMessage.create("Reset password request", request, getMethodName())
        ));

    String hash;
    try {
      hash = generateHash("user-service", getPrivateKey(secureInternalProperties.getPrivateKey()));
      var tokenClient =
          Feign.builder().client(okHttpClient).encoder(gsonEncoder)
              .decoder(gsonDecoder).target(TokenClient.class,
                  useEureka ? String.format("%s/api/v1", GetInstanceServer.get(
                      loadBalancer, userService
                  )) : userService);

      tokenClient.resetPassword(hash, input);
      LOGGER.info("Reset password success");
      return null;
    } catch (Exception e) {
      LOGGER.error("Error reset password: {}", e.getMessage());
      return renderErrorMsg(e);
    }
  }

  @PostMapping("/register")
  public UserResponse verifyAccount(@Valid @RequestBody RegisterRequest input,
      HttpServletRequest request) {

    LOGGER.info(
        gson.toJson(
            LogMessage.create("Register request from user %s".formatted(input.email()), request,
                getMethodName())
        ));

    String hash;
    try {
      hash = generateHash("user-service", getPrivateKey(secureInternalProperties.getPrivateKey()));
      LOGGER.info("Register success for user {}", input.email());
      var userClient =
          Feign.builder().client(okHttpClient).encoder(gsonEncoder)
              .decoder(gsonDecoder).target(UserClient.class,
                  useEureka ? String.format("%s/api/v1", GetInstanceServer.get(
                      loadBalancer, userService
                  )) : userService);

      return userClient.register(hash, input);
    } catch (Exception e) {
      LOGGER.error("Error registering user {}: {}", input.email(), e.getMessage());
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
