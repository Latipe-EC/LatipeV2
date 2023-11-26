package latipe.payment.consumer;

import com.google.gson.Gson;
import latipe.payment.services.PaymentService;
import latipe.payment.viewmodel.OrderMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service(value = "consumer-service")
@RequiredArgsConstructor
public class DataConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(DataConsumer.class);
  private final PaymentService paymentService;

  @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "${rabbitmq.queue.name}", durable = "true"),
      exchange = @Exchange(value = "${rabbitmq.exchange.name}"), key = "${rabbitmq.routing.key}"))
  public void listen(Message consumerRecord) {
    try {
      if (consumerRecord != null) {
        Gson gson = new Gson();
        OrderMessage message = gson.fromJson(new String(consumerRecord.getBody()),
            OrderMessage.class);
        if (message.status().equals(0)) {
          paymentService.handleOrderCreate(message);
        } else if (message.status().equals(4)) {
          paymentService.handleFinishShipping(message);
        } else if (message.status().equals(-1)) {
          paymentService.handleUserCancelOrder(message);
        }
      }

    } catch (RuntimeException e) {
      LOGGER.warn(e.getMessage());
    }
  }
}
