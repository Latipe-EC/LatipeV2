package latipe.schedule.property;

import java.util.Properties;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
@ConfigurationProperties(prefix = "spring.quartz")
public class QuartzProperties {

    private Properties properties;

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
