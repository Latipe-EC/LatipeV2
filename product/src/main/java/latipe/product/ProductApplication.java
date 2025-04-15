package latipe.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Product microservice.
 * This Spring Boot application handles product management, catalog, and product-related operations.
 * 
 * @author Latipe Development Team
 */
@SpringBootApplication
public class ProductApplication {

    /**
     * The main method that starts the Product microservice.
     *
     * @param args Command-line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(ProductApplication.class, args);
    }

}
