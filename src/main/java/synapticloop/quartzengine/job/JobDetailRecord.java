package synapticloop.quartzengine.job;

import java.util.Date;

public record JobDetailRecord(String name,
                              String group,
                              Date nextRunTime,
                              String status
) {}