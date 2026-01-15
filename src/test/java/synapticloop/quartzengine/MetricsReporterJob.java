package synapticloop.quartzengine;

import java.util.DoubleSummaryStatistics;
import java.util.Map;

import synapticloop.quartzengine.annotation.QuartzEngineJob;
import synapticloop.quartzengine.engine.QuartzEngine;
import synapticloop.quartzengine.metric.JobMetricStatistics;

public class MetricsReporterJob {

	/**
	 * This job runs every 5 minutes and prints a summary of system health
	 * and individual job performance.
	 */
	@QuartzEngineJob(cronExpression = "0/13 * * * * ?", group = "SystemAdmin")
	public void reportMetrics() {
		try {
			// 1. Get the stats from the singleton engine
			QuartzEngine engine = QuartzEngine.getInstance();
			JobMetricStatistics stats = engine.getStats();

			if (stats.getAllMetrics().isEmpty()) {
				System.out.println("[MetricsReporter] No execution history recorded yet.");
				return;
			}

			// 2. Print Global Health
			System.out.println("\n===============================================");
			System.out.println("        QUARTZ ENGINE HEALTH REPORT           ");
			System.out.println("===============================================");
			System.out.printf("Overall Success Rate: %.2f%%%n", stats.getSuccessPercentage());
			System.out.printf("Total Recorded Runs:  %d%n", stats.getTotalRuns());
			System.out.println("-----------------------------------------------");

			// 3. Print Per-Job Performance
			Map<String, DoubleSummaryStatistics> perJobStats = stats.getDurationStatsByJob();

			System.out.printf("%-40s | %-8s | %-8s | %-5s%n", "Job Name", "Avg ms", "Max ms", "Runs");
			System.out.println("-----------------------------------------------");

			perJobStats.forEach((name, s) -> {
				System.out.printf("%-40s | %-8.2f | %-8.0f | %-5d%n",
						name,
						s.getAverage(),
						s.getMax(),
						s.getCount()
				);
			});
			System.out.println("===============================================\n");

		} catch (Exception e) {
			System.err.println("MetricsReporterJob failed to retrieve statistics: " + e.getMessage());
		}
	}
}