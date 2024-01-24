package latipe.rating.configs;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "gate-way")
@Getter
@Setter
public class GateWayProperties {

  private String host;
  private String port;
}
