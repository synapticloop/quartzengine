package synapticloop.quartzengine.metric;

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
		synchronized (metrics) {
			if (metrics.isEmpty()) return 0.0;
			return 100.0 - getSuccessPercentage();
		}
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

	public void clear() {
		metrics.clear();
	}
}