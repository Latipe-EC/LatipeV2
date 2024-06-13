package latipe.user.configs;

import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.okhttp.OkHttpClient;
import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import java.io.IOException;
import latipe.user.annotations.ApiPrefixController;
import latipe.user.interceptor.GrpcServerRequestInterceptor;
import latipe.user.repositories.IUserRepository;
import latipe.user.services.user.UserGrpcService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class AppConfig implements WebMvcConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfig.class);
    private final IUserRepository userRepository;
    private final SecureInternalProperties secureInternalProperties;
    @Value("${grpc.port}")
    private int grpcServerPort;

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
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RequestContextListener requestContextListener() {
        return new RequestContextListener();
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

    @Bean
    public Server grpcServer() {

        var server = NettyServerBuilder.forPort(grpcServerPort)
            .intercept(new GrpcServerRequestInterceptor(secureInternalProperties))
            .addService(
                new UserGrpcService(userRepository))
            .build();

        try {
            server.start();
            LOGGER.info("Server GRPC started: " + grpcServerPort);
        } catch (IOException e) {
            LOGGER.error("Server GRPC did not start due to: " + e.getMessage());
        }

        return server;
    }
}
