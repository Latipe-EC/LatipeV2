package latipe.paymentpaypal.controllers;


import jakarta.validation.Valid;
import latipe.paymentpaypal.annotations.ApiPrefixController;
import latipe.paymentpaypal.annotations.Authenticate;
import latipe.paymentpaypal.services.PaypalService;
import latipe.paymentpaypal.viewmodel.CapturedPaymentVm;
import latipe.paymentpaypal.viewmodel.PaypalRequestPayment;
import latipe.paymentpaypal.viewmodel.RequestPayment;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@ApiPrefixController("/paypal")
public class PaypalController {
    private final PaypalService paypalService;

    @Authenticate
    @PostMapping(value = "/init")
    public PaypalRequestPayment createPayment(@Valid @RequestBody RequestPayment requestPayment) {
        return paypalService.createPayment(requestPayment);
    }
    @Authenticate
    @GetMapping(value = "/capture")
    public CapturedPaymentVm capturePayment(@RequestParam("token") String token) {
        return paypalService.capturePayment(token);
    }
    @Authenticate
    @GetMapping(value = "/cancel")
    public ResponseEntity<String> cancelPayment() {
        return ResponseEntity.ok("Payment cancelled");
    }
}