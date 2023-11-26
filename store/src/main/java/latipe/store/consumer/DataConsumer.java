package latipe.store.consumer;

import com.google.gson.Gson;
import latipe.store.exceptions.NotFoundException;
import latipe.store.repositories.IStoreRepository;
import latipe.store.viewmodel.StoreBillMessage;
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
public class DataConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(DataConsumer.class);
  private final IStoreRepository storeRepository;

  @RabbitListener(bindings = @QueueBinding(
      value = @Queue(value = "${rabbitmq.queue.name}",
          durable = "true"),
      exchange = @Exchange(value = "${rabbitmq.exchange.name}",
          type = "topic"), key = "${rabbitmq.routing.key}"))
  public void listen(Message consumerRecord) {
    try {
      if (consumerRecord != null) {
        Gson gson = new Gson();
        StoreBillMessage response = gson.fromJson(new String(consumerRecord.getBody()),
            StoreBillMessage.class);
        var store = storeRepository.findById(response.storeId()).orElseThrow(
            () -> new NotFoundException("Not found store")
        );

        store.setPoint(
            store.getPoint() + (response.amountReceived() + response.amountReceived()) / 10000);

        store.setEWallet(store.getEWallet() + response.amountReceived());
        storeRepository.save(store);

        LOGGER.info(
            "Store with %s has receive %s money".formatted(store.getId(), store.getEWallet()));
      }
    } catch (RuntimeException e) {
      LOGGER.warn(e.getMessage());
    }

  }
}
