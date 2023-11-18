package latipe.user.services.token;


import java.util.concurrent.CompletableFuture;
import latipe.user.request.VerifyAccountRequest;

public interface ITokenService {

  public CompletableFuture<?> validateVerify(VerifyAccountRequest token);


}

