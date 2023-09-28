package latipe.paymentpaypal.services;


import latipe.paymentpaypal.controllers.APIClient;
import latipe.paymentpaypal.viewmodel.CapturedPaymentVm;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PaymentService {

  private final APIClient apiClient;

  public void capturePaymentInfoToPaymentService(CapturedPaymentVm capturedPayment) {
    apiClient.capturePayment(capturedPayment);
  }
}
