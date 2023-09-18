package latipe.paymentpaypal.services;


import latipe.paymentpaypal.controllers.APIClient;
import latipe.paymentpaypal.viewmodel.CapturedPaymentVm;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service
@AllArgsConstructor
public class PaymentService {

    private final APIClient apiClient;
    public void capturePaymentInfoToPaymentService(CapturedPaymentVm capturedPayment) {
         apiClient.capturePayment(capturedPayment);
    }
}
