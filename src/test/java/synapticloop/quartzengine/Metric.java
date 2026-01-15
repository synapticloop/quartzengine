package synapticloop.quartzengine;

import org.quartz.SchedulerException;
import synapticloop.quartzengine.annotation.QuartzEngineJob;
import synapticloop.quartzengine.engine.QuartzEngine;
import synapticloop.quartzengine.metric.JobMetric;

public class Metric {
	@QuartzEngineJob(group = "metrics", cronExpression = "0/10 * * * * ?")
	public void printPerformanceReport() throws SchedulerException {
		System.out.println("\n--- Job Execution History (Last " + QuartzEngine.MAX_METRICS + ") ---");
		System.out.printf("%-40s | %-10s | %-8s | %-7s%n", "Job Name", "Duration", "Result", "Start Time");

		for (JobMetric m : QuartzEngine.getInstance().getStats().getAllMetrics()) {
			System.out.printf("%-40s | %-8dms | %-8s | %s%n",
					m.name(), m.durationMs(),
					m.successful() ? "SUCCESS" : "FAILED",
					m.startTime());
		}
	}}
