package latipe.user.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQProducer {

  private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQProducer.class);
  private final RabbitTemplate rabbitTemplate;


  public RabbitMQProducer(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  public void sendMessage(String message, String exchange, String routingKey) {
    LOGGER.info(String.format("Message sent -> %s", message));
    rabbitTemplate.convertAndSend(exchange, routingKey, message);
  }

}