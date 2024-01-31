package latipe.payment.producer;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RabbitMQProducer {

  private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQProducer.class);
  private final RabbitTemplate rabbitTemplate;

  public void sendMessage(String message, String exchange, String routingKey) {
    LOGGER.info(String.format("Message sent -> %s", message));
    rabbitTemplate.convertAndSend(exchange, routingKey, message);
  }

}