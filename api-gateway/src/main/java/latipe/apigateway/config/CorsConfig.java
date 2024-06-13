package latipe.apigateway.config;

import latipe.apigateway.interceptors.CustomHeaderInterceptor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class CorsConfig {

    @Bean
    public FilterRegistrationBean<CustomHeaderInterceptor> loggingFilter() {
        FilterRegistrationBean<CustomHeaderInterceptor> registrationBean
            = new FilterRegistrationBean<>();

        registrationBean.setFilter(new CustomHeaderInterceptor());
        registrationBean.addUrlPatterns("/**");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE); // Set the order

        return registrationBean;
    }

//  @Bean
//  public CorsWebFilter corsWebFilter() {
//    CorsConfiguration corsConfiguration = new CorsConfiguration();
//    corsConfiguration.setAllowCredentials(true);
//    corsConfiguration.addAllowedHeader("*");
//    corsConfiguration.addAllowedMethod("*");
//    corsConfiguration.addAllowedOrigin("*");
//    corsConfiguration.addExposedHeader(HttpHeaders.SET_COOKIE);
//    UrlBasedCorsConfigurationSource corsConfigurationSource = new UrlBasedCorsConfigurationSource();
//    corsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);
//    return new CorsWebFilter(corsConfigurationSource);
//  }

}
