package latipe.paymentpaypal.configs;

import com.nimbusds.jose.shaded.gson.JsonObject;
import com.nimbusds.jose.shaded.gson.JsonParser;
import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import latipe.paymentpaypal.Entity.PaymentProviderHelper;
import latipe.paymentpaypal.controllers.APIClient;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Configuration
@AllArgsConstructor
public class PaypalConfig {
    @Bean
    @RequestScope
    public PayPalHttpClient getPaypalClient(APIClient apiClient) {
        String additionalSettings = apiClient.getAdditionalSettings(PaymentProviderHelper.PAYPAL_PAYMENT_PROVIDER_ID);
        // Parse the additionalSettings field to extract clientId and clientSecret
        JsonObject settingsJson = JsonParser.parseString(additionalSettings).getAsJsonObject();
        String clientId = settingsJson.get("clientId").getAsString();
        String clientSecret = settingsJson.get("clientSecret").getAsString();
        String mode = settingsJson.get("mode").getAsString();
        if (mode.equals("sandbox"))
            // Create PayPalHttpClient with the retrieved clientId and clientSecret
            return new PayPalHttpClient(new PayPalEnvironment.Sandbox(clientId, clientSecret));
        return new PayPalHttpClient(new PayPalEnvironment.Live(clientId, clientSecret));
    }
}