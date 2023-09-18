package latipe.payment.services;


import latipe.payment.Entity.Payment;
import latipe.payment.repositories.PaymentRepository;
import latipe.payment.viewmodel.CapturedPayment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    @Async
    public CompletableFuture<CapturedPayment> capturePayment(CapturedPayment completedPayment) {
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
                    return CapturedPayment.fromModel(paymentRepository.save(payment));
                }
        );
    }
}
