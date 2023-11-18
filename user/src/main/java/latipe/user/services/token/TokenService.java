package latipe.user.services.token;

import com.google.gson.Gson;
import java.time.ZonedDateTime;
import java.util.concurrent.CompletableFuture;
import latipe.user.constants.CONSTANTS;
import latipe.user.constants.KeyType;
import latipe.user.exceptions.BadRequestException;
import latipe.user.exceptions.NotFoundException;
import latipe.user.mappers.UserMapper;
import latipe.user.producer.RabbitMQProducer;
import latipe.user.repositories.ITokenRepository;
import latipe.user.repositories.IUserRepository;
import latipe.user.request.ForgotPasswordRequest;
import latipe.user.request.RequestVerifyAccountRequest;
import latipe.user.request.ResetPasswordRequest;
import latipe.user.request.VerifyAccountRequest;
import latipe.user.utils.TokenUtils;
import latipe.user.viewmodel.ForgotPasswordMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TokenService implements ITokenService {

  private final ITokenRepository tokenRepository;
  private final IUserRepository userRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final RabbitMQProducer rabbitMQProducer;
  private final Gson gson;

  @Value("${rabbitmq.email.exchange.name}")
  private String exchange;
  @Value("${encryption.key}")
  private String ENCRYPTION_KEY;
  @Value("${rabbitmq.email.forgot-password-topic.routing.key}")
  private String routingForgotPasswordKey;
  @Value("${rabbitmq.email.user-register-topic.routing.key}")
  private String routingUserRegisterKey;
  @Value("${expiration.password-reset}")
  private Long expirationPasswordReset;

  @Async
  @Override
  @Transactional
  public CompletableFuture<?> validateVerify(VerifyAccountRequest request) {
    return CompletableFuture.supplyAsync(
        () -> {
          var id = TokenUtils.decodeToken(request.token(), ENCRYPTION_KEY);

          var tokenEntity = tokenRepository.findByIdAndUsedFalseAndExpiredAtAfterAndType(id,
                  ZonedDateTime.now(), KeyType.VERIFY_ACCOUNT)
              .orElseThrow(() -> new NotFoundException("Token invalid"));

          var user = userRepository.findById(tokenEntity.getUserId())
              .orElseThrow(() -> new NotFoundException("User not found"));
          user.setVerifiedAt(ZonedDateTime.now());
          tokenEntity.setUsed(true);
          tokenRepository.save(tokenEntity);
          userRepository.save(user);
          return null;
        }
    );
  }

  @Async
  @Override
  public CompletableFuture<?> verifyAccount(RequestVerifyAccountRequest request) {
    return CompletableFuture.supplyAsync(
        () -> {
          var user = userRepository.findByEmail(request.email())
              .orElseThrow(() -> new NotFoundException("User not found"));

          if (user.getIsDeleted()) {
            throw new NotFoundException("User not found");
          }

          if (user.getVerifiedAt() != null) {
            throw new BadRequestException("User already verified");
          }

          var token = tokenRepository.findByUserIdAndUsedFalseAndExpiredAtAfterAndType(
                  user.getId(),
                  ZonedDateTime.now(), KeyType.VERIFY_ACCOUNT)
              .orElse(null);

          if (token != null) {
            token.setUsed(true);
            tokenRepository.save(token);
          }

          token = userMapper.mapToToken(user.getId(), KeyType.VERIFY_ACCOUNT,
              ZonedDateTime.now().plusSeconds(expirationPasswordReset));

          token = tokenRepository.save(token);

          var tokenHash = TokenUtils.encodeToken(token.getId(), ENCRYPTION_KEY);

          // send mail verify account
          String message = gson.toJson(userMapper.mapToMessage(
              token.getUserId(), CONSTANTS.USER, user.getDisplayName(), user.getEmail(),
              null, tokenHash));
          rabbitMQProducer.sendMessage(message, exchange, routingUserRegisterKey);

          return null;
        }
    );
  }

  @Async
  @Override
  public CompletableFuture<?> forgotPassword(ForgotPasswordRequest request) {
    return CompletableFuture.supplyAsync(
        () -> {
          var user = userRepository.findByEmail(request.email())
              .orElseThrow(() -> new NotFoundException("User not found"));

          if (user.getIsDeleted()) {
            throw new NotFoundException("User not found");
          }
          if (user.getVerifiedAt() == null) {
            throw new NotFoundException("User not verified");
          }

          var token = tokenRepository.findByUserIdAndUsedFalseAndExpiredAtAfterAndType(
                  user.getId(),
                  ZonedDateTime.now(), KeyType.FORGOT_PASSWORD)
              .orElse(null);

          if (token != null) {
            token.setUsed(true);
            tokenRepository.save(token);
          }

          token = userMapper.mapToToken(user.getId(), KeyType.FORGOT_PASSWORD,
              ZonedDateTime.now().plusSeconds(expirationPasswordReset));

          token = tokenRepository.save(token);

          var message = gson.toJson(new ForgotPasswordMessage(
              user.getDisplayName(),
              user.getEmail(),
              TokenUtils.encodeToken(token.getId(), ENCRYPTION_KEY)
          ));

          rabbitMQProducer.sendMessage(message, exchange,
              routingForgotPasswordKey);

          return null;
        }
    );
  }

  @Async
  @Override
  public CompletableFuture<?> resetPassword(ResetPasswordRequest request) {
    return CompletableFuture.supplyAsync(
        () -> {
          var id = TokenUtils.decodeToken(request.token(), ENCRYPTION_KEY);
          var tokenEntity = tokenRepository.findByIdAndUsedFalseAndExpiredAtAfterAndType(id,
                  ZonedDateTime.now(), KeyType.FORGOT_PASSWORD)
              .orElseThrow(() -> new NotFoundException("Token invalid"));

          var user = userRepository.findById(tokenEntity.getUserId())
              .orElseThrow(() -> new NotFoundException("User not found"));

          user.setHashedPassword(passwordEncoder.encode(request.password()));
          tokenEntity.setUsed(true);
          tokenRepository.save(tokenEntity);
          userRepository.save(user);
          return null;
        }
    );
  }

}
