package latipe.user.configs;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

  @Value("${rabbitmq.email.user-register-topic.routing.key}")
  private String routingUserRegisterKey;
  @Value("${rabbitmq.email.delivery-register-topic.routing.key}")
  private String routingDeliveryRegisterKey;
  @Value("${rabbitmq.email.exchange.name}")
  private String exchangeName;

  @Bean
  public Queue userRegisterQueue() {
    return new Queue("test1", true);
  }

  @Bean
  public Queue deliveryRegisterQueue() {
    return new Queue("test2", true);
  }

  @Bean
  public TopicExchange exchange() {
    return new TopicExchange(exchangeName);
  }

  @Bean
  public Binding bindingUserRegister() {
    return BindingBuilder
        .bind(userRegisterQueue())
        .to(exchange())
        .with(routingUserRegisterKey);
  }

  @Bean
  public Binding bindingDeliveryRegister() {
    return BindingBuilder
        .bind(deliveryRegisterQueue())
        .to(exchange())
        .with(routingDeliveryRegisterKey);
  }

}
