package latipe.payment.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("payment_provider")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentProvider {

  @Id
  private String id;
  private boolean isEnabled;
  private String name;
  private String configureUrl;
  private String landingViewComponentName;
  private String additionalSettings;

}
