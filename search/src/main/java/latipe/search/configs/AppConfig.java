package latipe.search.configs;

import feign.Feign;
import feign.Logger;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.okhttp.OkHttpClient;
import latipe.search.annotations.ApiPrefixController;
import latipe.search.feign.ProductClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class AppConfig implements WebMvcConfigurer {

  private final GateWayProperties gateWayProperties;

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**").allowedMethods("*").allowedOrigins("*").allowedHeaders("*");
  }


  @Override
  public void configurePathMatch(PathMatchConfigurer configurer) {
    configurer.addPathPrefix("/api/v1",
        HandlerTypePredicate.forAnnotation(ApiPrefixController.class));
  }

  @Bean
  public RequestContextListener requestContextListener() {
    return new RequestContextListener();
  }

  @Bean
  public ProductClient getProductClient() {
    return Feign.builder().client(new OkHttpClient()).encoder(new GsonEncoder())
        .decoder(new GsonDecoder()).logLevel(Logger.Level.FULL).target(ProductClient.class,
            "%s:%s/api/v1".formatted(gateWayProperties.getHost(), gateWayProperties.getPort()));
  }
}
