package latipe.schedule.consumer;

import com.google.gson.Gson;
import latipe.schedule.jobs.CreateOrderJob;
import latipe.schedule.message.BaseMessage;
import latipe.schedule.message.OrderCanceledEvent;
import latipe.schedule.message.OrderCreatedEvent;
import latipe.schedule.utils.BuildKeyQuartz;
import lombok.RequiredArgsConstructor;
import org.quartz.DateBuilder;
import org.quartz.DateBuilder.IntervalUnit;
import org.quartz.JobBuilder;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
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
  private final Scheduler scheduler;

  @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "${rabbitmq.queue.name}", durable = "true"),
      exchange = @Exchange(value = "${rabbitmq.exchange.name}"), key = "${rabbitmq.routing.key}"))
  public void listen(Message consumerRecord) {
    try {
      if (consumerRecord != null) {
        Gson gson = new Gson();
        var baseMessage = gson.fromJson(new String(consumerRecord.getBody()),
            BaseMessage.class);
        if (baseMessage != null) {
          switch (baseMessage.type()) {
            case "ORDER_CREATED" -> {
              var message = gson.fromJson(baseMessage.message(),
                  OrderCreatedEvent.class);

              var job = JobBuilder.newJob(CreateOrderJob.class)
                  .withIdentity(
                      BuildKeyQuartz.buildJobKey(BuildKeyQuartz.CANCEL_ORDER, message.orderId()))
                  .usingJobData("orderId", message.orderId()).build();

              var triggerDate = DateBuilder.futureDate(2, IntervalUnit.DAY);
              var trigger = TriggerBuilder.newTrigger()
                  .withIdentity(BuildKeyQuartz.buildTriggerKey(BuildKeyQuartz.CANCEL_ORDER,
                      message.orderId())).startAt(triggerDate)
                  .build();
              scheduler.scheduleJob(job, trigger);
              LOGGER.info("Order created: {}", message.orderId());
            }
            case "ORDER_PAYMENT", "ORDER_CANCEL" -> {
              var message = gson.fromJson(baseMessage.message(),
                  OrderCanceledEvent.class);

              var jobKey = JobKey.jobKey(
                  BuildKeyQuartz.buildJobKey(BuildKeyQuartz.CANCEL_ORDER, message.orderId()));
              var triggerKey = TriggerKey.triggerKey(
                  BuildKeyQuartz.buildTriggerKey(BuildKeyQuartz.CANCEL_ORDER, message.orderId()));

              if (scheduler.checkExists(jobKey) && scheduler.checkExists(triggerKey)) {
                scheduler.unscheduleJob(triggerKey);
                scheduler.deleteJob(jobKey);
                LOGGER.info("Cancel job delete successfully");
              } else {
                LOGGER.error("Cannot find job or trigger");
                throw new RuntimeException("cannot-find-job-or-trigger");
              }

            }
            default -> LOGGER.warn("Unknown action received");
          }
        }
      }
    } catch (RuntimeException | SchedulerException e) {
      LOGGER.warn(e.getMessage());
    }
  }


}
