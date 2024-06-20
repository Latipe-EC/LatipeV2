package latipe.schedule.services;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import latipe.schedule.grpc.CreateScheduleRequest;
import latipe.schedule.grpc.CreateScheduleResponse;
import latipe.schedule.grpc.ScheduleServiceGrpc;
import latipe.schedule.jobs.CreateScheduleJob;
import latipe.schedule.utils.BuildKeyQuartz;
import latipe.schedule.utils.Const;
import lombok.RequiredArgsConstructor;
import org.quartz.JobBuilder;
import org.quartz.Scheduler;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
public class ScheduleGrpcService extends ScheduleServiceGrpc.ScheduleServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(ScheduleGrpcService.class);
    private final Scheduler scheduler;

    @Override
    public void createSchedule(CreateScheduleRequest request,
        StreamObserver<CreateScheduleResponse> responseObserver) {
        try {
            var randomId = UUID.randomUUID().toString();
            var dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            var requestDate = dateFormat.parse(request.getDeadline());
            var currentDate = new Date();

            if (requestDate.compareTo(currentDate) <= 0) {
                log.error("The provided date {} is not later than the current date",
                    request.getDeadline());
                responseObserver.onNext(
                    CreateScheduleResponse.newBuilder()
                        .setIsSuccess(false).build());
                responseObserver.onCompleted();
                return;
            }
            if (request.getType().equals(Const.ONE_TIME)) {
                var job = JobBuilder.newJob(CreateScheduleJob.class)
                    .withIdentity(BuildKeyQuartz.buildJobKey(request.getFrom(), randomId))
                    .usingJobData(Const.PRE_DATA, request.getData())
                    .usingJobData(Const.X_API_KEY, request.getXApiKey())
                    .usingJobData(Const.CALL_BACK_URL, request.getReplyOn()).build();

                var triggerDate = dateFormat.parse(request.getDeadline());

                var trigger = TriggerBuilder.newTrigger()
                    .withIdentity(BuildKeyQuartz.buildTriggerKey(request.getFrom(), randomId))
                    .startAt(triggerDate)
                    .build();

                scheduler.scheduleJob(job, trigger);
                log.info("Schedule job {} with id {} executed start from {}", request.getFrom(),
                    randomId, request.getDeadline());
            } else {
                log.info("TODO");
            }

            responseObserver.onNext(
                CreateScheduleResponse.newBuilder()
                    .setIsSuccess(true).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

}
