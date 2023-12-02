package latipe.schedule.jobs;

import com.google.gson.Gson;
import latipe.schedule.message.PublishMessage;
import latipe.schedule.producer.RabbitMQProducer;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateOrderJob implements Job {

  private final RabbitMQProducer publish;
  private final Gson gson;

  private static final Logger log = LoggerFactory.getLogger(CreateOrderJob.class);

  @Override
  public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
    var orderId = jobExecutionContext.getJobDetail().getJobDataMap().getString("orderId");
    var publishMessage = gson.toJson(new PublishMessage(
        "CANCEL_ORDER",
        orderId
    ), PublishMessage.class);
    publish.sendMessage(publishMessage);
    log.info("Create Order with id {} executed", orderId);
  }
}
