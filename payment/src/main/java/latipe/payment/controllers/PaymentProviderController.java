package latipe.payment.controllers;

import latipe.payment.annotations.ApiPrefixController;
import latipe.payment.annotations.RequiresAuthorization;
import latipe.payment.services.PaymentProviderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequiredArgsConstructor
@ApiPrefixController("/payment-providers")
public class PaymentProviderController {
    private final PaymentProviderService paymentProviderService;

    @GetMapping("/{id}/additional-settings")
    public CompletableFuture<String> getAdditionalSettings(@PathVariable("id") String id) {
        return paymentProviderService.getAdditionalSettingsByPaymentProviderId(id);
    }
}