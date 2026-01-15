package synapticloop.quartzengine.listener;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GlobalJobListener implements JobListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalJobListener.class.getName());

	@Override
	public String getName() {
		return "GlobalExceptionHandler";
	}

	@Override
	public void jobToBeExecuted(JobExecutionContext context) {}

	@Override
	public void jobExecutionVetoed(JobExecutionContext context) {}

	@Override
	public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
		if (jobException != null) {
			LOGGER.error("Job: {} failed, error was: {}",
					context.getJobDetail().getKey(),
					jobException.getMessage(),
					jobException);
		}
	}
}