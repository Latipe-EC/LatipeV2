package latipe.user.repositories;

import java.time.ZonedDateTime;
import java.util.Optional;
import latipe.user.constants.KeyType;
import latipe.user.entity.Token;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ITokenRepository extends MongoRepository<Token, String> {

    Optional<Token> findByIdAndUsedFalseAndExpiredAtAfterAndType(String key, ZonedDateTime now,
        KeyType action);

    Optional<Token> findByUserIdAndUsedFalseAndExpiredAtAfterAndType(String userId,
        ZonedDateTime now, KeyType action);

}
