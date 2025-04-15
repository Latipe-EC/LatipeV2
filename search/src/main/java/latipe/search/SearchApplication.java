package latipe.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Search microservice.
 * This Spring Boot application handles search functionality across the platform.
 * 
 * @author Latipe Development Team
 */
@SpringBootApplication
public class SearchApplication {

    /**
     * The main method that starts the Search microservice.
     *
     * @param args Command-line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(SearchApplication.class, args);
    }

}
