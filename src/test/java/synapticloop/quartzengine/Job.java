package synapticloop.quartzengine;

import synapticloop.quartzengine.annotation.QuartzEngineJob;
import synapticloop.quartzengine.annotation.QuartzEngineJobRunNow;

public class Job {
	private int runCount = 0; // This will persist because the class is a singleton!

	@QuartzEngineJobRunNow
	@QuartzEngineJob(cronExpression = "0/10 * * * * ?")
	public void track() {
		runCount++;
		System.out.println("This job has run " + runCount + " times.");
	}
}
