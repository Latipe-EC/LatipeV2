package latipe.payment.services;


import java.util.concurrent.CompletableFuture;
import latipe.payment.Entity.Payment;
import latipe.payment.repositories.PaymentRepository;
import latipe.payment.request.CapturedPaymentRequest;
import latipe.payment.response.CapturedPaymentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;

  public PaymentService(PaymentRepository paymentRepository) {
    this.paymentRepository = paymentRepository;
  }

  @Async
  public CompletableFuture<CapturedPaymentResponse> capturePayment(
      CapturedPaymentRequest completedPayment) {
        return CompletableFuture.supplyAsync(
                () -> {
                    Payment payment = Payment.builder()
                            .checkoutId(completedPayment.checkoutId())
                            .orderId(completedPayment.orderId())
                            .paymentStatus(completedPayment.paymentStatus())
                            .paymentFee(completedPayment.paymentFee())
                            .paymentMethod(completedPayment.paymentMethod())
                            .amount(completedPayment.amount())
                            .failureMessage(completedPayment.failureMessage())
                            .gatewayTransactionId(completedPayment.gatewayTransactionId())
                            .build();
                  return CapturedPaymentResponse.fromModel(paymentRepository.save(payment));
                }
        );
    }
}
