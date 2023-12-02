package latipe.payment.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.concurrent.CompletableFuture;
import latipe.payment.annotations.ApiPrefixController;
import latipe.payment.annotations.Authenticate;
import latipe.payment.annotations.SecureInternalPhase;
import latipe.payment.request.CapturedPaymentRequest;
import latipe.payment.request.PayByPaypalRequest;
import latipe.payment.request.PayOrderRequest;
import latipe.payment.response.CapturedPaymentResponse;
import latipe.payment.response.CheckPaymentOrderResponse;
import latipe.payment.response.UserCredentialResponse;
import latipe.payment.services.PaymentService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestController
@ApiPrefixController("payment")
@RequiredArgsConstructor
public class PaymentController {

  private final PaymentService paymentService;

  @PostMapping("/capture-payment")
  public CompletableFuture<CapturedPaymentResponse> capturePayment(
      @Valid @RequestBody CapturedPaymentRequest capturedPaymentRequest) {
    return paymentService.capturePayment(capturedPaymentRequest);
  }

  @PostMapping("/pay")
  public CompletableFuture<Void> validPayment(
      @Valid @RequestBody PayOrderRequest request) {
    return paymentService.payOrder(request);
  }


  @Authenticate
  @PostMapping("/payment-order/{orderId}")
  public CompletableFuture<CheckPaymentOrderResponse> getPayment(
      @PathVariable String orderId) {
    return paymentService.getPaymentOrder(orderId);
  }

  @Authenticate
  @PostMapping("/capture-payments/paypal")
  public CompletableFuture<Void> payByPaypal(
      @Valid @RequestBody PayByPaypalRequest request) {
    HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    UserCredentialResponse userCredential = (UserCredentialResponse) (req.getAttribute("user"));
    return paymentService.payByPaypal(request, userCredential);
  }

  @Authenticate
  @GetMapping("/check-order-paypal/{orderId}")
  public CompletableFuture<CheckPaymentOrderResponse> checkOrderPaypal(
      @PathVariable String orderId) {
    HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    UserCredentialResponse userCredential = (UserCredentialResponse) (req.getAttribute("user"));
    return paymentService.checkOrderPaypal(orderId, userCredential);
  }

  @SecureInternalPhase
  @GetMapping("/check-order-internal/{orderId}")
  public CompletableFuture<CheckPaymentOrderResponse> checkPaymentInternal( @PathVariable String orderId){
    return paymentService.checkPaymentInternal(orderId);
  }
}
