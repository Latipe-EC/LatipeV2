package latipe.payment.services;


import java.util.concurrent.CompletableFuture;
import latipe.payment.entity.PaymentProvider;
import latipe.payment.exceptions.NotFoundException;
import latipe.payment.repositories.PaymentProviderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentProviderService {

  private final PaymentProviderRepository paymentProviderRepository;

  @Async
  public CompletableFuture<String> getAdditionalSettingsByPaymentProviderId(
      String paymentProviderId) {
    return CompletableFuture.supplyAsync(
        () -> {
          PaymentProvider paymentProvider = paymentProviderRepository.findById(paymentProviderId)
              .orElseThrow(()
                  -> new NotFoundException("PAYMENT_PROVIDER_NOT_FOUND", paymentProviderId));
          return paymentProvider.getAdditionalSettings();
        }
    );

  }
}
