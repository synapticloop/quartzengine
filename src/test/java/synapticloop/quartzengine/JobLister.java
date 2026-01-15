package synapticloop.quartzengine;

import synapticloop.quartzengine.annotation.QuartzEngineJob;
import synapticloop.quartzengine.annotation.QuartzEngineJobRunNow;
import synapticloop.quartzengine.engine.QuartzEngine;
import synapticloop.quartzengine.job.JobDetailRecord;

import java.util.List;
public class JobLister {
	private int runCount = 0; // This will persist because the class is a singleton!

	@QuartzEngineJobRunNow
	@QuartzEngineJob(cronExpression = "0/10 * * * * ?")
	public void list() {
		try {
			// Access the singleton engine
			QuartzEngine engine = QuartzEngine.getInstance();

			// Get the list of job records
			List<JobDetailRecord> upcomingJobs = engine.listScheduledJobs();

			System.out.println("--- Current System Schedule ---");
			for (JobDetailRecord detail : upcomingJobs) {
				System.out.println("Job: " + detail.name() + " | Next run: " + detail.nextRunTime());
			}

		} catch (Exception e) {
			System.err.println("Could not access the Quartz Engine: " + e.getMessage());
		}
	}
}
