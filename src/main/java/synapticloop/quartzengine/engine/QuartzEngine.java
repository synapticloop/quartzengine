package synapticloop.quartzengine.engine;

/* Copyright (c) 2026 synapticloop.
 * All rights reserved.
 *
 * This source code and any derived binaries are covered by the terms and
 * conditions of the Licence agreement ("the Licence").  You may not use this
 * source code or any derived binaries except in compliance with the Licence.
 * A copy of the Licence is available in the file named LICENCE shipped with
 * this source code or binaries.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * Licence for the specific language governing permissions and limitations
 * under the Licence.
 */

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import synapticloop.quartzengine.annotation.QuartzJob;
import synapticloop.quartzengine.annotation.QuartzJobRunNow;
import synapticloop.quartzengine.job.JobDetailRecord;
import synapticloop.quartzengine.job.MethodInvokerJob;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>The {@code QuartzEngine} serves as a centralized manager for the Quartz Scheduler,
 * providing an automated, annotation-driven approach to job registration.</p>
 *
 * <p>This engine implements a Singleton pattern to ensure that only one instance of the
 * scheduler and its associated thread pool exist within the JVM. It utilizes the
 * {@code Reflections} library to scan specified packages for methods decorated with
 * {@link QuartzJob}.</p>
 *
 * <p>Key Features:</p>
 *
 * <ul>
 * <li><b>Singleton Job Instances:</b> Ensures each job class is instantiated only once,
 * allowing jobs to share state or resources.</li>
 * <li><b>Package Caching:</b> Tracks scanned packages to prevent redundant classpath
 * crawling and duplicate job registration.</li>
 * <li><b>Dynamic Loading:</b> Supports adding new job packages at runtime via
 * {@link #scanPackages(String...)}.</li>
 * </ul>
 * </p>
 *
 * @author synapticloop
 */
public class QuartzEngine {
	public static final String TARGET_OBJECT = "targetObject";
	public static final String TARGET_METHOD = "targetMethod";
	public static final String PARAMS_ARRAY = "paramsArray";
	public static final String TRIGGER = "Trigger";

	private static QuartzEngine instance;
	private final Scheduler scheduler;

	// Global cache for Job class instances
	private static final Map<Class<?>, Object> instanceCache = new ConcurrentHashMap<>();

	// Cache to track scanned packages to prevent redundant work
	private static final Set<String> scannedPackages = ConcurrentHashMap.newKeySet();

	private QuartzEngine() throws SchedulerException {
		this.scheduler = StdSchedulerFactory.getDefaultScheduler();
		this.scheduler.start();
	}

	/**
	 * <p>Retrieves the global instance of the {@code QuartzEngine}. If the engine has not
	 * been initialized, this method will start the Quartz Scheduler and perform
	 * the initial classpath scan.</p>
	 *
	 * <p>This method is thread-safe. If called multiple times with the same packages,
	 * the engine will skip redundant scans using its internal package cache.</p>
	 *
	 * @param packagesToScan A variadic array of package names (e.g., "com.app.jobs")
	 * to scan for {@literal @}QuartzJob annotations.
	 * @return The singleton {@code QuartzEngine} instance.
	 * @throws org.quartz.SchedulerException If the Quartz Scheduler fails to initialize
	 * or start.
	 */
	public static synchronized QuartzEngine getInstance(String... packagesToScan) throws SchedulerException {
		if (instance == null) {
			instance = new QuartzEngine();
		}

		if (packagesToScan != null && packagesToScan.length > 0) {
			instance.scanPackages(packagesToScan);
		}

		return instance;
	}

	/**
	 * Public method to add new packages. Skips any package that has already been scanned.
	 */
	public void scanPackages(String... packagesToScan) {
		for (String pkg : packagesToScan) {
			// add() returns true if the set did not already contain the package
			if (scannedPackages.add(pkg)) {
				System.out.println("[QuartzEngine] New package detected. Scanning: " + pkg);
				scanAndRegister(pkg);
			} else {
				System.out.println("[QuartzEngine] Skipping already scanned package: " + pkg);
			}
		}
	}

	private void scanAndRegister(String packageToScan) {
		Reflections reflections = new Reflections(packageToScan, Scanners.MethodsAnnotated);
		Set<Method> jobMethods = reflections.getMethodsAnnotatedWith(QuartzJob.class);

		for (Method method : jobMethods) {
			try {
				Class<?> clazz = method.getDeclaringClass();

				Object jobInstance = instanceCache.computeIfAbsent(clazz, k -> {
					try {
						return k.getDeclaredConstructor().newInstance();
					} catch (Exception e) {
						System.err.println("[QuartzEngine] Failed to instantiate " + k.getName()
								+ ". Ensure it has a public no-arg constructor.");
						return null;
					}
				});

				if (jobInstance != null) {
					registerJob(jobInstance, method);
				}
			} catch (Exception e) {
				System.err.println("[QuartzEngine] Error processing method: " + method.getName());
				e.printStackTrace();
			}
		}
	}

	private void registerJob(Object jobInstance, Method method) throws SchedulerException {
		QuartzJob config = method.getAnnotation(QuartzJob.class);
		JobKey jobKey = new JobKey(method.getName(), config.group());

		// Final safety check: skip if the job name/group is already in Quartz
		if (scheduler.checkExists(jobKey)) {
			return;
		}

		JobDetail job = JobBuilder.newJob(MethodInvokerJob.class)
				.withIdentity(jobKey)
				.build();

		// Explicitly putting the String[] into the Map
		job.getJobDataMap().put(TARGET_OBJECT, jobInstance);
		job.getJobDataMap().put(TARGET_METHOD, method);
		job.getJobDataMap().put(PARAMS_ARRAY, config.parameters()); // String[] stored here

		Trigger trigger = TriggerBuilder.newTrigger()
				.withIdentity(method.getName() + TRIGGER, config.group())
				.withSchedule(CronScheduleBuilder.cronSchedule(config.cronExpression()))
				.build();

		scheduler.scheduleJob(job, trigger);

		if (method.isAnnotationPresent(QuartzJobRunNow.class)) {
			System.out.println("[QuartzEngine] QuartzJobRunNow detected. Triggering " + method.getName());
			scheduler.triggerJob(jobKey);
		}
	}

	/**
	 * <p>Retrieves a comprehensive snapshot of all currently registered jobs within the
	 * Quartz Scheduler.</p>
	 *
	 * <p>This method performs the following operations:</p>
	 *
	 * <ul>
	 *  <li>Iterates through all known Job Groups.</li>
	 *  <li>Queries each Group for its associated {@link JobKey}s.</li>
	 *  <li>Retrieves the primary {@link Trigger} for each job to determine its next fire time.</li>
	 *  <li>Checks the current {@link org.quartz.Trigger.TriggerState} to identify if a job is
	 * NORMAL, PAUSED, BLOCKED, or in an ERROR state.</li>
	 * </ul>
	 * <p>Because this method returns a List of immutable {@link JobDetailRecord} records, the
	 * data can be safely passed to UI components, REST controllers, or logging utilities
	 * without risking side effects on the active Scheduler.</p>
	 *
	 * @return A {@link List} of {@link JobDetailRecord} records representing every scheduled task. Returns an empty list
	 * 		if no jobs are registered or if a {@link SchedulerException} occurs.
	 *
	 * @see JobDetailRecord
	 * @see org.quartz.Scheduler#getJobGroupNames()
	 */
	public List<JobDetailRecord> listScheduledJobs() {
		List<JobDetailRecord> jobList = new ArrayList<>();

		try {
			for (String groupName : scheduler.getJobGroupNames()) {
				for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {

					// 1. Get Trigger info
					List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
					Trigger firstTrigger = triggers.isEmpty() ? null : triggers.get(0);

					// 2. Determine Next Run and Status
					java.util.Date nextRun = (firstTrigger != null) ? firstTrigger.getNextFireTime() : null;

					String status = "UNKNOWN";
					if (firstTrigger != null) {
						Trigger.TriggerState state = scheduler.getTriggerState(firstTrigger.getKey());
						status = state.name();
					}

					// 3. Create the record and add to list
					jobList.add(new JobDetailRecord(
							jobKey.getName(),
							jobKey.getGroup(),
							nextRun,
							status
					));
				}
			}
		} catch (SchedulerException e) {
			System.err.println("[QuartzEngine] Error retrieving job list: " + e.getMessage());
		}

		return jobList;
	}

	public void shutdown() throws SchedulerException {
		scheduler.shutdown(true);
	}
}