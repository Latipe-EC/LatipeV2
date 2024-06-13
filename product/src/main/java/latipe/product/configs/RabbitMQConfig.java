package latipe.product.configs;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.order.queue}")
    private String queueOrder;

    @Value("${rabbitmq.order.exchange}")
    private String exchangeOrder;

    @Value("${rabbitmq.order.commit}")
    private String routingKeyCommit;

    @Value("${rabbitmq.order.rollback}")
    private String routingKeyRollback;

    @Value("${rabbitmq.queue.name}")
    private String queue;

    @Value("${rabbitmq.exchange.name}")
    private String exchange;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    @Bean
    public Queue queueOrder() {
        return new Queue(queueOrder);
    }

    @Bean
    public Queue queue() {
        return new Queue(queue);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchange);
    }

    @Bean
    public TopicExchange exchangeOrder() {
        return new TopicExchange(exchangeOrder);
    }

    @Bean
    public Binding binding() {
        return BindingBuilder
            .bind(queue())
            .to(exchange())
            .with(routingKey);
    }

    @Bean
    public Binding bindingOrderCommit() {
        return BindingBuilder
            .bind(queueOrder())
            .to(exchangeOrder())
            .with(routingKeyCommit);
    }

    @Bean
    public Binding bindingOrderRollback() {
        return BindingBuilder
            .bind(queueOrder())
            .to(exchangeOrder())
            .with(routingKeyRollback);
    }
}
