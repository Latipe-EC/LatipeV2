package latipe.store.consumer;

import com.google.gson.Gson;
import latipe.store.constants.Action;
import latipe.store.exceptions.NotFoundException;
import latipe.store.repositories.IStoreRepository;
import latipe.store.viewmodel.RatingMessage;
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
    public void listenStore(Message consumerRecord) {
        try {
            if (consumerRecord != null) {
                Gson gson = new Gson();
                StoreBillMessage response = gson.fromJson(new String(consumerRecord.getBody()),
                    StoreBillMessage.class);
                var store = storeRepository.findById(response.storeId()).orElseThrow(
                    () -> new NotFoundException("Not found store")
                );

                store.setPoint(
                    store.getPoint()
                        + (response.amountReceived() + response.amountReceived()) / 10000);

                store.setEWallet(store.getEWallet() + response.amountReceived());
                storeRepository.save(store);

                LOGGER.info(
                    "Store with %s has receive %s money".formatted(store.getId(),
                        store.getEWallet()));
            }
        } catch (RuntimeException e) {
            LOGGER.warn(e.getMessage());
        }

    }


    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = "${rabbitmq.queue.rating.name}",
            durable = "true"),
        exchange = @Exchange(value = "${rabbitmq.exchange.rating.name}"), key = "${rabbitmq.routing.rating.key}"))
    public void listenRating(Message consumerRecord) {
        LOGGER.info("Received message from rating");
        try {
            if (consumerRecord != null) {
                Gson gson = new Gson();
                var ratingMessage = gson.fromJson(new String(consumerRecord.getBody()),
                    RatingMessage.class);
                var id = ratingMessage.storeId();
                if (id != null) {
                    String op = ratingMessage.op();
                    LOGGER.info("Received action [%s] with id [%s]".formatted(op, id));
                    if (op != null) {
                        switch (op) {
                            case Action.CREATE -> storeRepository.findById(id).ifPresent(store -> {
                                var rating = store.getRatings();
                                rating.set(ratingMessage.rating() - 1,
                                    rating.get(ratingMessage.rating() - 1) + 1);
                                storeRepository.save(store);
                            });

                            case Action.UPDATE -> storeRepository.findById(id).ifPresent(store -> {
                                var rating = store.getRatings();
                                rating.set(ratingMessage.rating() - 1,
                                    rating.get(ratingMessage.rating() - 1) + 1);
                                rating.set(ratingMessage.oldRating() - 1,
                                    rating.get(ratingMessage.oldRating() - 1) - 1);
                                storeRepository.save(store);
                            });

                            case Action.DELETE -> storeRepository.findById(id).ifPresent(store -> {
                                var rating = store.getRatings();
                                rating.set(ratingMessage.rating() - 1,
                                    rating.get(ratingMessage.rating() - 1) - 1);
                                storeRepository.save(store);
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
