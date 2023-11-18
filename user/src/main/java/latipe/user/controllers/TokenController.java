package latipe.user.controllers;

import jakarta.validation.Valid;
import java.util.concurrent.CompletableFuture;
import latipe.user.annotations.ApiPrefixController;
import latipe.user.annotations.SecureInternalPhase;
import latipe.user.request.ForgotPasswordRequest;
import latipe.user.request.RequestVerifyAccountRequest;
import latipe.user.request.ResetPasswordRequest;
import latipe.user.request.VerifyAccountRequest;
import latipe.user.services.token.ITokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@ApiPrefixController("/tokens")
@RequiredArgsConstructor
public class TokenController {

  private final ITokenService tokenService;

  @SecureInternalPhase
  @PostMapping(value = "/finish-verify-account", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<?> validateVerify(
      @RequestBody @Valid VerifyAccountRequest request
  ) {
    return tokenService.validateVerify(request);
  }

  @SecureInternalPhase
  @PostMapping(value = "/verify-account", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<?> verifyAccount(
      @RequestBody RequestVerifyAccountRequest request
  ) {
    return tokenService.verifyAccount(request);
  }

  @SecureInternalPhase
  @PostMapping(value = "/forgot-password", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<?> forgotPassword(
      @RequestBody ForgotPasswordRequest request
  ) {
    return tokenService.forgotPassword(request);
  }


  @SecureInternalPhase
  @PostMapping(value = "/reset-password", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<?> resetPassword(
      @RequestBody ResetPasswordRequest request
  ) {
    return tokenService.resetPassword(request);
  }


}
