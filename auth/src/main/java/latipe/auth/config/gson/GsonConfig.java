package latipe.auth.config.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.time.ZonedDateTime;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GsonConfig {

  @Bean
  public Gson gson() {
    return new GsonBuilder().registerTypeAdapter(ZonedDateTime.class,
        new ZonedDateTimeTypeAdapter()).setPrettyPrinting().create();
  }
}
