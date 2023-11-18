package latipe.user.controllers;

import jakarta.validation.Valid;
import java.util.concurrent.CompletableFuture;
import latipe.user.annotations.ApiPrefixController;
import latipe.user.annotations.SecureInternalPhase;
import latipe.user.request.VerifyAccountRequest;
import latipe.user.services.token.ITokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@ApiPrefixController("/tokens")
@RequiredArgsConstructor
public class TokenController {

  private final ITokenService tokenService;

  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/verify-account", produces = MediaType.APPLICATION_JSON_VALUE)
  @SecureInternalPhase
  public CompletableFuture<?> validateVerify(
      @RequestBody @Valid VerifyAccountRequest request
  ) {
    return tokenService.validateVerify(request);
  }

}
