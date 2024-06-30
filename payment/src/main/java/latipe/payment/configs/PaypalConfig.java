package latipe.payment.configs;

import static latipe.payment.constants.CONSTANTS.PAYPAL_CONFIG_CODE;

import com.nimbusds.jose.shaded.gson.JsonObject;
import com.nimbusds.jose.shaded.gson.JsonParser;
import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import latipe.payment.exceptions.NotFoundException;
import latipe.payment.repositories.PaymentProviderRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
@Slf4j
public class PaypalConfig {

    private final PaymentProviderRepository paymentProviderRepository;

    @Bean
    public PayPalHttpClient getPaypalClient() {
        var paymentProvider = paymentProviderRepository.findByCode("PaypalPayment")
            .orElseThrow(()
                -> new NotFoundException("PAYMENT_PROVIDER_NOT_FOUND", PAYPAL_CONFIG_CODE));
        // Parse the additionalSettings field to extract clientId and clientSecret
        log.info("config paypal success");

        JsonObject settingsJson = JsonParser.parseString(paymentProvider.getAdditionalSettings())
            .getAsJsonObject();
        String clientId = settingsJson.get("clientId").getAsString();
        String clientSecret = settingsJson.get("clientSecret").getAsString();
        String mode = settingsJson.get("mode").getAsString();
        // Create PayPalHttpClient with the retrieved clientId and clientSecret
        return new PayPalHttpClient(new PayPalEnvironment.Sandbox(clientId, clientSecret));
    }
}