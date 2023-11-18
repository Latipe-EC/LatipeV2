package latipe.user.services.token;


import java.util.concurrent.CompletableFuture;
import latipe.user.request.ForgotPasswordRequest;
import latipe.user.request.RequestVerifyAccountRequest;
import latipe.user.request.ResetPasswordRequest;
import latipe.user.request.VerifyAccountRequest;

public interface ITokenService {

  CompletableFuture<?> validateVerify(VerifyAccountRequest token);

  CompletableFuture<?> forgotPassword(ForgotPasswordRequest token);

  CompletableFuture<?> verifyAccount(RequestVerifyAccountRequest token);

  CompletableFuture<?> resetPassword(ResetPasswordRequest token);

}

