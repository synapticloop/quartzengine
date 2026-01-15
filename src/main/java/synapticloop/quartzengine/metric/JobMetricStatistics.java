package synapticloop.quartzengine.metric;

import synapticloop.quartzengine.metric.JobMetric;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class JobMetricStatistics {
	private static final int MAX_METRICS = 100;
	// Using a synchronized list or CopyOnWriteArrayList for thread-safe live updates
	private final List<JobMetric> metrics = Collections.synchronizedList(new LinkedList<>());

	/**
	 * Adds a new metric to the collection and maintains the maximum size.
	 */
	public void addMetric(JobMetric metric) {
		synchronized (metrics) {
			if (metrics.size() >= MAX_METRICS) {
				metrics.remove(0); // Remove oldest
			}
			metrics.add(metric);
		}
	}

	public List<JobMetric> getMetrics() {
		synchronized (metrics) {
			return new ArrayList<>(metrics);
		}
	}

	public double getSuccessPercentage() {
		synchronized (metrics) {
			if (metrics.isEmpty()) return 0.0;
			long successCount = metrics.stream().filter(JobMetric::successful).count();
			return (successCount * 100.0) / metrics.size();
		}
	}

	public double getFailurePercentage() {
		if (metrics.isEmpty()) return 0.0;
		return 100.0 - getSuccessPercentage();
	}

	public Map<String, DoubleSummaryStatistics> getDurationStatsByJob() {
		synchronized (metrics) {
			return metrics.stream()
					.collect(Collectors.groupingBy(
							JobMetric::name,
							Collectors.summarizingDouble(JobMetric::durationMs)
					));
		}
	}
}