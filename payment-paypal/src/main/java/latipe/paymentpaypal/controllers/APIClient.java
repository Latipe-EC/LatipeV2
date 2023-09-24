package latipe.paymentpaypal.controllers;

import latipe.paymentpaypal.viewmodel.CapturedPaymentVm;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "API", url = "http://localhost:8181/api/v1")
public interface APIClient {
    @PostMapping(value = "/auth/validate-token")
    UserCredentialResponse getCredential(@RequestBody() TokenRequest accessToken);

    @PostMapping(value = "/payment/capture-payment")
    CapturedPaymentVm capturePayment(CapturedPaymentVm capturedPayment);

    @GetMapping(value = "/payment-providers/{id}/additional-settings")
    String getAdditionalSettings(@PathVariable String id);
}
