package latipe.search.consumer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import latipe.search.constants.Action;
import latipe.search.services.ProductSyncDataService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProductSyncDataConsumer {

  private ProductSyncDataService productSyncDataService;
  private static final Logger LOGGER = LoggerFactory.getLogger(ProductSyncDataConsumer.class);

  @RabbitListener(queues = {"${rabbitmq.queue.name}"})
  public void listen(String consumerRecord) {
    if (consumerRecord != null) {
      JsonObject jsonObject = JsonParser.parseString(consumerRecord).getAsJsonObject();
      JsonElement keyObject = jsonObject.get("key");
      if (keyObject != null) {
        JsonElement valueObject = jsonObject.get("op");
        if (valueObject != null) {
          String action = valueObject.getAsString();
          String id = keyObject.getAsString();
          switch (action) {
            case Action.CREATE -> productSyncDataService.createProduct(id);
            case Action.UPDATE -> productSyncDataService.updateProduct(id);
            case Action.DELETE -> productSyncDataService.deleteProduct(id);
            case Action.BAN -> productSyncDataService.banProduct(id);
            default -> LOGGER.warn("Unknown action received: " + action);
          }
        }
      }
    }
  }

}
