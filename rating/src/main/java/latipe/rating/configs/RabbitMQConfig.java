package latipe.rating.configs;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

  @Value("${rabbitmq.queue.name_order}")
  private String queueOrder;

  @Value("${rabbitmq.queue.name_product}")
  private String queueProduct;

  @Value("${rabbitmq.queue.name_store}")
  private String queueStore;

  @Value("${rabbitmq.exchange.name}")
  private String exchange;

  @Value("${rabbitmq.routing.key}")
  private String routingKey;

  // spring bean for rabbitmq queue
  @Bean
  public Queue queueOrder() {
    return QueueBuilder.durable(queueOrder).build();
  }

  @Bean
  public Queue queueProduct() {
    return QueueBuilder.durable(queueProduct).build();
  }

  @Bean
  public Queue queueStore() {
    return QueueBuilder.durable(queueStore).build();
  }

  // spring bean for rabbitmq exchange
  @Bean
  public DirectExchange exchange() {
    return new DirectExchange(exchange);
  }


  // binding between queue and exchange using routing key
  @Bean
  public Binding bindingOrder() {
    return BindingBuilder
        .bind(queueOrder())
        .to(exchange())
        .with(routingKey);
  }

  @Bean
  public Binding bindingProduct() {
    return BindingBuilder
        .bind(queueProduct())
        .to(exchange())
        .with(routingKey);
  }

  @Bean
  public Binding bindingStore() {
    return BindingBuilder
        .bind(queueStore())
        .to(exchange())
        .with(routingKey);
  }
}
