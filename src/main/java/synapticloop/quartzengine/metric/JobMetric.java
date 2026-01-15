package synapticloop.quartzengine.metric;

import java.time.Instant;

/**
 * Represents a historical snapshot of a single job execution.
 */
public record JobMetric(
		String name,
		String group,
		Instant startTime,
		long durationMs,
		boolean successful,
		String errorMessage
) {}