package latipe.cart.consumer;

import com.google.gson.Gson;
import java.util.concurrent.ExecutionException;
import latipe.cart.services.Cart.ICartService;
import latipe.cart.viewmodel.UpdateCartAfterOrderVm;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(CartConsumer.class);
  private final ICartService cartService;

  @RabbitListener(bindings = @QueueBinding(
      value = @Queue(value = "${rabbitmq.queue.name}",
          durable = "true"),
      exchange = @Exchange(value = "${rabbitmq.exchange.name}",
          type = "topic"), key = "${rabbitmq.routing.key}"))
  public void listen(Message consumerRecord) {
    try {
      if (consumerRecord != null) {
        Gson gson = new Gson();
        UpdateCartAfterOrderVm cartIdVmList = gson.fromJson(new String(consumerRecord.getBody()),
            UpdateCartAfterOrderVm.class);
        cartService.removeCartItemAfterOrder(cartIdVmList).get();
      }
    } catch (RuntimeException | ExecutionException | InterruptedException e) {
      LOGGER.warn(e.getMessage());
    }
  }
}
