package latipe.payment.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.concurrent.CompletableFuture;
import latipe.payment.annotations.ApiPrefixController;
import latipe.payment.annotations.Authenticate;
import latipe.payment.annotations.RequiresAuthorization;
import latipe.payment.annotations.SecureInternalPhase;
import latipe.payment.dtos.PagedResultDto;
import latipe.payment.entity.enumeration.EStatusFilter;
import latipe.payment.request.CapturedPaymentRequest;
import latipe.payment.request.PayByPaypalRequest;
import latipe.payment.request.PayOrderRequest;
import latipe.payment.request.ValidWithdrawPaypalRequest;
import latipe.payment.request.WithdrawPaypalRequest;
import latipe.payment.response.CapturedPaymentResponse;
import latipe.payment.response.CheckPaymentOrderResponse;
import latipe.payment.response.PaymentResponse;
import latipe.payment.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ApiPrefixController("payment")
@RequiredArgsConstructor
public class PaymentController {

  private final PaymentService paymentService;

  @PostMapping("/capture-payment")
  public CompletableFuture<CapturedPaymentResponse> capturePayment(
      @Valid @RequestBody CapturedPaymentRequest capturedPaymentRequest,
      HttpServletRequest request) {
    return paymentService.capturePayment(capturedPaymentRequest, request);
  }

  @PostMapping("/pay")
  public CompletableFuture<Void> validPayment(
      @Valid @RequestBody PayOrderRequest input, HttpServletRequest request) {
    return paymentService.payOrder(input, request);
  }


  @Authenticate
  @PostMapping("/payment-order/{orderId}")
  public CompletableFuture<CheckPaymentOrderResponse> getPayment(
      @PathVariable String orderId, HttpServletRequest request) {
    return paymentService.getPaymentOrder(orderId, request);
  }

  @Authenticate
  @PostMapping("/capture-payments/paypal")
  public CompletableFuture<Void> payByPaypal(
      @Valid @RequestBody PayByPaypalRequest input, HttpServletRequest request) {

    return paymentService.payByPaypal(input, request);
  }

  @Authenticate
  @GetMapping("/check-order-paypal/{orderId}")
  public CompletableFuture<CheckPaymentOrderResponse> checkOrderPaypal(
      @PathVariable String orderId, HttpServletRequest request) {

    return paymentService.checkOrderPaypal(orderId, request);
  }

  @SecureInternalPhase
  @GetMapping("/check-order-internal/{orderId}")
  public CompletableFuture<CheckPaymentOrderResponse> checkPaymentInternal(
      @PathVariable String orderId, HttpServletRequest request) {
    return paymentService.checkPaymentInternal(orderId, request);
  }

  @RequiresAuthorization("VENDOR")
  @PostMapping("/withdraw-paypal")
  public CompletableFuture<Void> withdrawPaypal(
      @Valid @RequestBody
      WithdrawPaypalRequest input, HttpServletRequest request) {

    return paymentService.withdrawPaypal(input, request);
  }

  @RequiresAuthorization("VENDOR")
  @PostMapping("/valid-withdraw-paypal")
  public CompletableFuture<Void> validWithdrawPaypal(
      @Valid @RequestBody
      ValidWithdrawPaypalRequest input, HttpServletRequest request) {

    return paymentService.validWithdrawPaypal(input, request);
  }

  @RequiresAuthorization("ADMIN")
  @GetMapping("/paginate")
  public CompletableFuture<PagedResultDto<PaymentResponse>> getPaginate(
      @RequestParam(defaultValue = "") String keyword,
      @RequestParam(defaultValue = "0") Long skip,
      @RequestParam(defaultValue = "12") Integer size,
      @RequestParam(defaultValue = "ALL") EStatusFilter statusFilter, HttpServletRequest request) {
    return paymentService.getPaginate(keyword, skip, size, statusFilter, request);
  }
}
