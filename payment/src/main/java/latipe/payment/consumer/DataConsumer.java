package latipe.payment.consumer;

import static latipe.payment.constants.CONSTANTS.URL;
import static latipe.payment.utils.GenTokenInternal.generateHash;
import static latipe.payment.utils.GenTokenInternal.getPrivateKey;

import com.google.gson.Gson;
import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.okhttp.OkHttpClient;
import latipe.payment.Entity.Payment;
import latipe.payment.Entity.enumeration.EPaymentMethod;
import latipe.payment.Entity.enumeration.EPaymentStatus;
import latipe.payment.configs.SecureInternalProperties;
import latipe.payment.exceptions.NotFoundException;
import latipe.payment.feign.UserClient;
import latipe.payment.repositories.PaymentRepository;
import latipe.payment.request.CancelOrderRequest;
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
  private final SecureInternalProperties secureInternalProperties;

  @RabbitListener(bindings = @QueueBinding(
      value = @Queue(value = "${rabbitmq.queue.name}",
          durable = "true"),
      exchange = @Exchange(value = "${rabbitmq.exchange.name}",
          type = "direct"), key = "${rabbitmq.routing.key}"))
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
          payment.setPaymentMethod(
              message.paymentMethod() == 1 ? EPaymentMethod.COD : EPaymentMethod.PAYPAL);
          paymentRepository.save(payment);
        } else if (message.status().equals(4)) {
          var payment = paymentRepository.findByOrderId(message.orderUuid()).orElseThrow(
              () -> new NotFoundException("Not found payment")
          );
          payment.setPaymentStatus(EPaymentStatus.COMPLETED);
          paymentRepository.save(payment);
        } else if (message.status().equals(-1)) {
          var payment = paymentRepository.findByOrderId(message.orderUuid()).orElseThrow(
              () -> new NotFoundException("Not found payment")
          );

          if (!payment.getPaymentMethod().equals(EPaymentMethod.COD)
              && payment.getPaymentStatus().equals(EPaymentStatus.COMPLETED)
          ) {
            // call api refund money and minus point
            var userClient = Feign.builder().client(new OkHttpClient()).encoder(new GsonEncoder())
                .decoder(new GsonDecoder()).logLevel(feign.Logger.Level.FULL)
                .target(UserClient.class, URL);

            String hash;
            try {
              hash = generateHash("user-service",
                  getPrivateKey(secureInternalProperties.getPrivateKey()));
            } catch (Exception e) {
              throw new RuntimeException(e);
            }

            userClient.cancelOrder(hash,
                new CancelOrderRequest(payment.getUserId(), payment.getAmount()));
            LOGGER.info("refund money for user with userId [%s] with money %s".formatted(
                payment.getUserId(), payment.getAmount()
            ));
          }

          payment.setPaymentStatus(EPaymentStatus.CANCELLED);
          paymentRepository.save(payment);
        }

      }
    } catch (RuntimeException e) {
      LOGGER.warn(e.getMessage());
    }
  }
}
