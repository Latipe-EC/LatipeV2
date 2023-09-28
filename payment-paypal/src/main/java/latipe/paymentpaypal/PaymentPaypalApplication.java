package latipe.paymentpaypal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class PaymentPaypalApplication {

  public static void main(String[] args) {
    SpringApplication.run(PaymentPaypalApplication.class, args);
  }
}
