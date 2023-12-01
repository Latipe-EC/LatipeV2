package latipe.payment.controllers;

import jakarta.validation.Valid;
import java.util.concurrent.CompletableFuture;
import latipe.payment.annotations.ApiPrefixController;
import latipe.payment.annotations.Authenticate;
import latipe.payment.request.CapturedPaymentRequest;
import latipe.payment.request.PayOrderRequest;
import latipe.payment.response.CapturedPaymentResponse;
import latipe.payment.response.CheckPaymentOrderResponse;
import latipe.payment.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
  @GetMapping("/payment-order/{orderId}")
  public CompletableFuture<CheckPaymentOrderResponse> getPayment(
      @PathVariable String orderId) {
    return paymentService.getPaymentOrder(orderId);
  }
}
