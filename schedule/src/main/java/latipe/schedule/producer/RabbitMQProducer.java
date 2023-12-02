package latipe.schedule.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQProducer {

  private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQProducer.class);
  private final RabbitTemplate rabbitTemplate;
  @Value("${rabbitmq.exchange.name}")
  private String exchange;
  @Value("${rabbitmq.routing.key}")
  private String routingKey;

  public RabbitMQProducer(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  public void sendMessage(String message) {
    LOGGER.info(String.format("Message sent -> %s", message));
    rabbitTemplate.convertAndSend(exchange, routingKey, message);
  }
}