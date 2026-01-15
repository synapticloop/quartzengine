package synapticloop.quartzengine.metric;

import synapticloop.quartzengine.metric.JobMetric;

import java.util.*;
import java.util.stream.Collectors;

public class JobMetricStatistics {
	private static final int MAX_METRICS = 100;
	private final List<JobMetric> metrics = Collections.synchronizedList(new LinkedList<>());

	/**
	 * Adds a metric and ensures the list doesn't exceed MAX_METRICS.
	 */
	public void addMetric(JobMetric metric) {
		synchronized (metrics) {
			if (metrics.size() >= MAX_METRICS) {
				metrics.remove(0);
			} else {
				metrics.add(metric);
			}
		}
	}

	/** Returns the raw list of all captured metrics. */
	public List<JobMetric> getAllMetrics() {
		return(new ArrayList<>(metrics));
	}

	public int getTotalRuns() {
		return metrics.size();
	}

	public long getSuccessCount() {
		return metrics.stream().filter(JobMetric::successful).count();
	}

	public long getFailureCount() {
		return metrics.size() - getSuccessCount();
	}

	public double getSuccessPercentage() {
		synchronized (metrics) {
		if (metrics.isEmpty()) return 0.0;
			long successCount = metrics.stream().filter(JobMetric::successful).count();
			return (successCount * 100.0) / metrics.size();
		}
	}

	public double getFailurePercentage() {
		synchronized (metrics) {
		if (metrics.isEmpty()) return 0.0;
			return 100.0 - getSuccessPercentage();
		}
	}

	/** Returns a map of statistics grouped by Job Name. */
	public Map<String, DoubleSummaryStatistics> getDurationStatsByJob() {
		synchronized (metrics) {
			return metrics.stream()
					.collect(Collectors.groupingBy(
							JobMetric::name,
							Collectors.summarizingDouble(JobMetric::durationMs)
					));
		}
	}

	/** Finds the single slowest execution in the current history. */
	public Optional<JobMetric> getSlowestExecution() {
		return metrics.stream().max(Comparator.comparingLong(JobMetric::durationMs));
	}

	public void clear() {
		metrics.clear();
	}

}