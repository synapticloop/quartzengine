import org.quartz.SchedulerException;
import synapticloop.quartzengine.engine.QuartzEngine;
import synapticloop.quartzengine.job.JobDetailRecord;

public class TestMain {
	public static void main(String[] args) throws SchedulerException, InterruptedException {
		QuartzEngine engine = QuartzEngine.getInstance("synapticloop.quartzengine");
		for (JobDetailRecord listScheduledJob : engine.listScheduledJobs()) {
			System.out.println(listScheduledJob);
		}
		// Check the console output here!
		System.out.println("Quartz Engine Singleton is running.");
		Thread.currentThread().join();
	}
}
