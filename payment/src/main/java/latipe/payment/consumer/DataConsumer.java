package latipe.payment.consumer;

import static latipe.payment.constants.CONSTANTS.ORDER_CANCEL_BY_ADMIN;
import static latipe.payment.constants.CONSTANTS.ORDER_CANCEL_BY_DELI;
import static latipe.payment.constants.CONSTANTS.ORDER_CANCEL_BY_STORE;
import static latipe.payment.constants.CONSTANTS.ORDER_CANCEL_BY_USER;
import static latipe.payment.constants.CONSTANTS.ORDER_SHIPPING_FINISH;
import static latipe.payment.constants.CONSTANTS.ORDER_SYSTEM_PROCESS;

import com.google.gson.Gson;
import latipe.payment.producer.RabbitMQProducer;
import latipe.payment.services.PaymentService;
import latipe.payment.viewmodel.OrderMessage;
import latipe.payment.viewmodel.OrderReplyMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final PaymentService paymentService;
    private final Gson gson;
    private final RabbitMQProducer rabbitMQProducer;

    @Value("${rabbitmq.order.reply}")
    private String replyRoutingKey;
    @Value("${rabbitmq.order.exchange}")
    private String exchange;

    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "${rabbitmq.order.queue-commit}", durable = "true"),
        exchange = @Exchange(value = "${rabbitmq.order.exchange}", type = "topic"), key = "${rabbitmq.order.commit}"))
    public void listenCommitOrder(Message consumerRecord) {
        LOGGER.info("Receive message ");
        String orderId = null;
        int orderStatus = 0;
        try {
            if (consumerRecord != null) {
                OrderMessage message = gson.fromJson(new String(consumerRecord.getBody()),
                    OrderMessage.class);
                orderId = message.orderId();
                orderStatus = message.status();

                switch (orderStatus) {

                    case ORDER_SYSTEM_PROCESS ->
                        paymentService.handleOrderCreate(message); //create purchase payment

                    case ORDER_SHIPPING_FINISH -> {    //the order is finished shipping
                        var payment = paymentService.handleFinishShipping(message);
                        LOGGER.info(
                            "User finish shipping order: %s with amount %s".formatted(
                                payment.getOrderId(),
                                payment.getAmount()));
                    }

                    case ORDER_CANCEL_BY_USER, ORDER_CANCEL_BY_STORE,
                         ORDER_CANCEL_BY_DELI, ORDER_CANCEL_BY_ADMIN -> {  //the order is cancel
                        paymentService.handleUserCancelOrder(message);
                        LOGGER.info("User cancel order: {}", message.orderId());
                    }
                }
            }
        } catch (RuntimeException e) {
            // rollback create order
            if (orderId != null && orderStatus == ORDER_SYSTEM_PROCESS) {
                rabbitMQProducer.sendMessage(gson.toJson(
                    OrderReplyMessage.create(0, orderId)), exchange, replyRoutingKey);
            }
            LOGGER.warn(e.getMessage());
        }
    }

    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "${rabbitmq.order.queue-rollback}", durable = "true"),
        exchange = @Exchange(value = "${rabbitmq.order.exchange}", type = "topic"), key = "${rabbitmq.order.rollback}"))
    public void listenRollbackOrder(Message consumerRecord) {
        String orderId;
        try {
            if (consumerRecord != null) {
                var message = gson.fromJson(new String(consumerRecord.getBody()),
                    OrderMessage.class);
                orderId = message.orderId();
                paymentService.handleRollbackOrder(orderId);
            }
        } catch (RuntimeException e) {
            // TODO if fail send message to scheduler to rollback order later

//            if (e.getMessage().contains("Withdraw")) {
//                // send message to scheduler to create withdraw
//            } else {
//                // send message to scheduler to re-refund
//            }
            LOGGER.warn(e.getMessage());
        }
    }

}
