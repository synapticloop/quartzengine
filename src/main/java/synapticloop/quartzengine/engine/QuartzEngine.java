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
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import synapticloop.quartzengine.annotation.QuartzJob;
import synapticloop.quartzengine.annotation.QuartzJobRunNow;
import synapticloop.quartzengine.job.MethodInvokerJob;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
	 * Retrieves the Singleton instance and scans provided packages if they haven't been seen yet.
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

	public void shutdown() throws SchedulerException {
		scheduler.shutdown(true);
	}
}