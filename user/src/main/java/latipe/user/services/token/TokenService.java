package latipe.user.services.token;

import java.time.ZonedDateTime;
import java.util.concurrent.CompletableFuture;
import latipe.user.constants.KeyType;
import latipe.user.exceptions.NotFoundException;
import latipe.user.repositories.ITokenRepository;
import latipe.user.repositories.IUserRepository;
import latipe.user.request.VerifyAccountRequest;
import latipe.user.utils.TokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService implements ITokenService {

  private final ITokenRepository tokenRepository;
  private final IUserRepository userRepository;

  @Value("${encryption.key}")
  private String ENCRYPTION_KEY;

  @Async
  @Override
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

}
