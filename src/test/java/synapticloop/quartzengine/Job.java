package synapticloop.quartzengine;

import synapticloop.quartzengine.annotation.QuartzJob;
import synapticloop.quartzengine.annotation.QuartzJobRunNow;

public class Job {
	private int runCount = 0; // This will persist because the class is a singleton!

	@QuartzJobRunNow
	@QuartzJob(cronExpression = "0/10 * * * * ?")
	public void track() {
		runCount++;
		System.out.println("This job has run " + runCount + " times.");
	}
}
