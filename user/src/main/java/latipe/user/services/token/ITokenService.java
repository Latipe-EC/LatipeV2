package latipe.user.services.token;


import jakarta.servlet.http.HttpServletRequest;
import java.util.concurrent.CompletableFuture;
import latipe.user.request.ForgotPasswordRequest;
import latipe.user.request.RequestVerifyAccountRequest;
import latipe.user.request.ResetPasswordRequest;
import latipe.user.request.VerifyAccountRequest;

public interface ITokenService {

    CompletableFuture<?> validateVerify(VerifyAccountRequest token, HttpServletRequest request);

    CompletableFuture<?> forgotPassword(ForgotPasswordRequest token, HttpServletRequest request);

    CompletableFuture<?> verifyAccount(RequestVerifyAccountRequest token,
        HttpServletRequest request);

    CompletableFuture<?> resetPassword(ResetPasswordRequest token, HttpServletRequest request);

}

