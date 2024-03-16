package latipe.auth.configs;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "secure-internal")
@Getter
@Setter
public class SecureInternalProperties {

  private String publicKey;
  private String privateKey;
}
