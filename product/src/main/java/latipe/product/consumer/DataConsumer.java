package latipe.product.consumer;

import com.google.gson.Gson;
import latipe.product.constants.Action;
import latipe.product.producer.RabbitMQProducer;
import latipe.product.repositories.IProductRepository;
import latipe.product.request.UpdateProductQuantityRequest;
import latipe.product.viewmodel.RatingMessage;
import latipe.product.viewmodel.UpdateProductQuantityVm;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DataConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(DataConsumer.class);
  private final IProductRepository productRepository;
 private final RabbitMQProducer rabbitMQProducer;
  @Value("${rabbitmq.order.exchange}")
  private String exchange;
  @Value("${rabbitmq.order.reply}")
  private String replyRoutingKey;

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

  @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "${rabbitmq.order.queue}",
      durable = "true"), exchange = @Exchange(value = "${rabbitmq.order.exchange}", type = ExchangeTypes.TOPIC),
      key = "${rabbitmq.order.commit}"))
  public void listenCommitOrder(Message consumerRecord) {
    LOGGER.info("Received message from order");
    try {
      if (consumerRecord != null) {
        Gson gson = new Gson();
        var request = gson.fromJson(new String(consumerRecord.getBody()),
            UpdateProductQuantityVm.class);
        var prods = productRepository.findAllByIdsAndStoreId(request.items().stream().map(
            UpdateProductQuantityRequest::productId
        ).toList(), request.storeId());

        for (var item : request.items()) {
          var prod = prods.stream().filter(p -> p.getId().equals(item.productId())
          ).findFirst();
          if (prod.isPresent()) {
            var classification = prod.get().getProductClassifications().stream().filter(
                c -> c.getId().equals(item.optionId())
            ).findFirst();
            if (classification.isPresent()) {
              if (classification.get().getQuantity() >= item.quantity()) {
                classification.get()
                    .setQuantity(classification.get().getQuantity() - item.quantity());
              } else {
                // product out of stock
                rabbitMQProducer.sendMessage(exchange, replyRoutingKey, request.orderId());
                return;
              }
            } else {
              // not found classification
              rabbitMQProducer.sendMessage(exchange, replyRoutingKey, request.orderId());
              return;
            }
          } else {
            // not found product
            rabbitMQProducer.sendMessage(exchange, replyRoutingKey, request.orderId());
            return;
          }
        }
        productRepository.saveAll(prods);
        LOGGER.info("Update quantity success");
      }
    } catch (RuntimeException e) {
      LOGGER.error("error processing message: {}", e.getMessage());
    }
  }

  @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "${rabbitmq.order.queue}",
      durable = "true"), exchange = @Exchange(value = "${rabbitmq.order.exchange}", type = ExchangeTypes.TOPIC),
      key = "${rabbitmq.order.rollback}"))
  public void listenRollbackOrder(Message consumerRecord) {
    LOGGER.info("Received message from order");
    try {
      if (consumerRecord != null) {
        Gson gson = new Gson();
        var request = gson.fromJson(new String(consumerRecord.getBody()),
            UpdateProductQuantityVm.class);
        var prods = productRepository.findAllByIdsAndStoreId(request.items().stream().map(
            UpdateProductQuantityRequest::productId
        ).toList(), request.storeId());

        for (var item : request.items()) {
          var prod = prods.stream().filter(p -> p.getId().equals(item.productId())
          ).findFirst();
          if (prod.isPresent()) {
            var classification = prod.get().getProductClassifications().stream().filter(
                c -> c.getId().equals(item.optionId())
            ).findFirst();
            classification.ifPresent(productClassification -> productClassification
                .setQuantity(productClassification.getQuantity() + item.quantity()));
          }
        }
        productRepository.saveAll(prods);
        LOGGER.info("Rollback quantity success");
      }
    } catch (RuntimeException e) {
      LOGGER.error("error processing message: {}", e.getMessage());
    }
  }
}
