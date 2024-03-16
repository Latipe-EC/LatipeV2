package latipe.auth.configs;


import com.google.gson.Gson;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.okhttp.OkHttpClient;
import latipe.auth.annotations.ApiPrefixController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AppConfig implements WebMvcConfigurer {

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
        .allowedMethods("*")
        .allowedOrigins("*")
        .allowedHeaders("*");
  }

  @Override
  public void configurePathMatch(PathMatchConfigurer configurer) {
    configurer.addPathPrefix("/api/v1",
        HandlerTypePredicate.forAnnotation(ApiPrefixController.class));
  }

  @Bean
  public Gson getGson() {
    return new Gson();
  }

  @Bean
  public GsonDecoder getGsonDecoder() {
    return new GsonDecoder();
  }

  @Bean
  public GsonEncoder getGsonEncoder() {
    return new GsonEncoder();
  }

  @Bean
  public OkHttpClient okHttpClient() {
    return new OkHttpClient();
  }

}
