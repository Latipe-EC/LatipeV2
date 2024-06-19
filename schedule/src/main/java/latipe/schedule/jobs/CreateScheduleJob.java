package latipe.schedule.jobs;

import java.io.IOException;
import latipe.schedule.utils.Const;
import okhttp3.Request;
import okhttp3.Response;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CreateScheduleJob implements Job {

    private static final Logger log = LoggerFactory.getLogger(CreateScheduleJob.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        var data = jobExecutionContext.getJobDetail().getJobDataMap().getString("data");
        var callbackUrl = jobExecutionContext.getJobDetail().getJobDataMap()
            .getString("call_back_url");
        var headerKey = jobExecutionContext.getJobDetail().getJobDataMap().getString("header_key");

        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
        var req = new Request.Builder()
            .url(callbackUrl)
            .addHeader(Const.X_API_KEY, headerKey)
            .post(okhttp3.RequestBody.create(data, okhttp3.MediaType.parse("application/json")))
            .build();

        Response response = null;
        try {
            response = client.newCall(req).execute();
            log.info("Call [URL: {}] with response {}", callbackUrl, response.message());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
