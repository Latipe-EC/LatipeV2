package latipe.payment.consumer;

import com.google.gson.Gson;
import latipe.payment.Entity.Payment;
import latipe.payment.Entity.enumeration.EPaymentStatus;
import latipe.payment.exceptions.NotFoundException;
import latipe.payment.repositories.PaymentRepository;
import latipe.payment.viewmodel.OrderMessage;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
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
  private final PaymentRepository paymentRepository;

  @RabbitListener(bindings = @QueueBinding(
      value = @Queue(value = "${rabbitmq.queue.name}",
          durable = "true"),
      exchange = @Exchange(value = "${rabbitmq.exchange.name}",
          type = "topic"), key = "${rabbitmq.routing.key}"))
  public void listen(Message consumerRecord) {
    try {
      if (consumerRecord != null) {
        Gson gson = new Gson();
        OrderMessage message = gson.fromJson(new String(consumerRecord.getBody()),
            OrderMessage.class);
        if (message.status().equals(0)) {
          Payment payment = new Payment();
          payment.setId((new ObjectId()).toString());
          payment.setOrderId(message.orderUuid());
          payment.setAmount(message.amount());
          payment.setPaymentStatus(EPaymentStatus.PENDING);
          payment.setUserId(message.userRequest().userId());
          paymentRepository.save(payment);
        } else if (message.status().equals(4)) {
          var payment = paymentRepository.findByOrderId(message.orderUuid()).orElseThrow(
              () -> new NotFoundException("Not found payment")
          );
          payment.setPaymentStatus(EPaymentStatus.COMPLETED);
          paymentRepository.save(payment);
        }

      }
    } catch (RuntimeException e) {
      LOGGER.warn(e.getMessage());
    }
  }
}
