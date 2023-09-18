package latipe.payment.controllers;

import jakarta.validation.Valid;
import latipe.payment.annotations.ApiPrefixController;

import latipe.payment.annotations.RequiresAuthorization;
import latipe.payment.services.PaymentService;
import latipe.payment.viewmodel.CapturedPayment;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@ApiPrefixController("payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/capture-payment")
    public CompletableFuture<CapturedPayment> capturePayment(@Valid @RequestBody CapturedPayment capturedPayment) {
        return paymentService.capturePayment(capturedPayment);
    }
}
