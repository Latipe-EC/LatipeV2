package latipe.schedule.configs;

import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import java.io.IOException;
import latipe.schedule.interceptors.GrpcServerRequestInterceptor;
import latipe.schedule.services.ScheduleGrpcService;
import lombok.RequiredArgsConstructor;
import org.quartz.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AppConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfig.class);
    private final SecureInternalProperties secureInternalProperties;
    private final Scheduler scheduler;
    @Value("${grpc.port}")
    private int grpcServerPort;

    @Bean
    public Server grpcServer() {
        var server = NettyServerBuilder.forPort(grpcServerPort)
            .intercept(new GrpcServerRequestInterceptor(secureInternalProperties))
            .addService(
                new ScheduleGrpcService(scheduler) {
                })
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
