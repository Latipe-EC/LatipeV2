package latipe.media.configs;

import java.io.IOException;
import java.net.ServerSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

public class PortInitializer implements
    ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PortInitializer.class);

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {

        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        int port = environment.getProperty("server.port", Integer.class, findRandomPort());
        if (isPortInUse(port)) {
            int randomPort = findRandomPort();
            LOGGER.info("[Media Service]: Port {} is already in use, so using random port {}", port,
                randomPort);
            System.setProperty("server.port", String.valueOf(randomPort));
            return;
        }
        LOGGER.info("[Media Service]: Port {} is available", port);
    }

    private boolean isPortInUse(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            return false;
        } catch (IOException e) {
            return true;
        }
    }

    private int findRandomPort() {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
} 
