package latipe.auth.config;

import latipe.auth.middlewares.GlobalApiLoggerInterceptor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AppConfig implements WebMvcConfigurer {
    private final GlobalApiLoggerInterceptor globalApiLoggerInterceptor;

    public AppConfig(GlobalApiLoggerInterceptor globalApiLoggerInterceptor) {
        this.globalApiLoggerInterceptor = globalApiLoggerInterceptor;
    }
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(globalApiLoggerInterceptor);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedMethods("*").allowedOrigins("*").allowedHeaders("*");
    }
    @Bean
    public ModelMapper getModelMapper() {
        return new ModelMapper();
    }
}
