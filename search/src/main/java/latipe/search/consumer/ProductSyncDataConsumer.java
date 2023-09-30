package latipe.search.consumer;

import com.google.gson.Gson;
import latipe.search.constants.Action;
import latipe.search.services.ProductSyncDataService;
import latipe.search.viewmodel.ProductMessageVm;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProductSyncDataConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProductSyncDataConsumer.class);
  private ProductSyncDataService productSyncDataService;

  @RabbitListener(queues = {"${rabbitmq.queue.name}"})
  public void listen(Message consumerRecord) {
    try {
      if (consumerRecord != null) {
        Gson gson = new Gson();
        ProductMessageVm productMessage = gson.fromJson(new String(consumerRecord.getBody()),
            ProductMessageVm.class);
        String id = productMessage.id();
        if (id != null) {
          String op = productMessage.op();
          LOGGER.info("Received action [%s] with id [%s]".formatted(op, id));
          if (op != null) {
            switch (op) {
              case Action.CREATE -> productSyncDataService.createProduct(id);
              case Action.UPDATE -> productSyncDataService.updateProduct(id);
              case Action.DELETE -> productSyncDataService.deleteProduct(id);
              case Action.BAN -> productSyncDataService.banProduct(id);
              default -> LOGGER.warn("Unknown action received");
            }
          }
        }
      }
    } catch (RuntimeException e) {
      LOGGER.warn(e.getMessage());
    }

  }
}
