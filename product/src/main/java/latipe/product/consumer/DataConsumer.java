package latipe.product.consumer;

import com.google.gson.Gson;
import latipe.product.constants.Action;
import latipe.product.repositories.IProductRepository;
import latipe.product.viewmodel.RatingMessage;
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
  private final IProductRepository productRepository;

  // TODO miss message when use topic exchange remember test again
  @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "${rabbitmq.rating.queue.name}",
      durable = "true"), exchange = @Exchange(value = "${rabbitmq.rating.exchange.name}"),
      key = "${rabbitmq.rating.routing.key}"))
  public void listen(Message consumerRecord) {
    LOGGER.info("Received message from rating");
    try {
      if (consumerRecord != null) {
        Gson gson = new Gson();
        var ratingMessage = gson.fromJson(new String(consumerRecord.getBody()),
            RatingMessage.class);
        var id = ratingMessage.productId();
        if (id != null) {
          String op = ratingMessage.op();
          LOGGER.info("Received action [%s] with id [%s]".formatted(op, id));
          if (op != null) {
            switch (op) {
              case Action.CREATE -> productRepository.findById(id).ifPresent(product -> {
                var rating = product.getRatings();
                rating.set(ratingMessage.rating() - 1, rating.get(ratingMessage.rating() - 1) + 1);
                productRepository.save(product);
              });

              case Action.UPDATE -> productRepository.findById(id).ifPresent(product -> {
                var rating = product.getRatings();
                rating.set(ratingMessage.rating() - 1,
                    rating.get(ratingMessage.rating() - 1) + 1);
                rating.set(ratingMessage.oldRating() - 1,
                    rating.get(ratingMessage.oldRating() - 1) - 1);
                productRepository.save(product);
              });

              case Action.DELETE -> productRepository.findById(id).ifPresent(product -> {
                var rating = product.getRatings();
                rating.set(ratingMessage.rating() - 1,
                    rating.get(ratingMessage.rating() - 1) - 1);
                productRepository.save(product);
              });
              default -> LOGGER.warn("Unknown action received");
            }
          }
        }
      }
    } catch (RuntimeException e) {
      LOGGER.error("error processing message: {}", e.getMessage());
    }
  }
}
