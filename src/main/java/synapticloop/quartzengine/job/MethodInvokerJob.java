package synapticloop.quartzengine.job;

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
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import synapticloop.quartzengine.engine.QuartzEngine;

import java.lang.reflect.Method;

public class MethodInvokerJob implements Job {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodInvokerJob.class);

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobKey key = context.getJobDetail().getKey();

		LOGGER.debug("Job: {} executing", key);

		try {
			JobDataMap dataMap = context.getMergedJobDataMap();
			Object targetObject = dataMap.get(QuartzEngine.TARGET_OBJECT);
			Method method = (Method) dataMap.get(QuartzEngine.TARGET_METHOD);
			String[] params = (String[]) dataMap.get(QuartzEngine.PARAMS_ARRAY);

			if (targetObject == null || method == null) {
				throw new JobExecutionException("Target Object or Method was missing from JobDataMap!");
			}

			LOGGER.debug("Attempting to invoke {}.{}", targetObject.getClass().getSimpleName(), method.getName());

			// Check if method is accessible (handles package-private or protected if necessary)
			if (!method.canAccess(targetObject)) {
				method.setAccessible(true);
			}

			// Invoke the method
			if (method.getParameterCount() == 1 && method.getParameterTypes()[0].equals(JobExecutionContext.class)) {
				method.invoke(targetObject, context);
			} else {
				method.invoke(targetObject);
			}

			LOGGER.debug("Successfully executed: {}", method.getName());

		} catch (Exception e) {
			LOGGER.error("Failed to execute job: {}", key);
			// We wrap the exception so the GlobalJobListener catches it
			throw new JobExecutionException(e);
		}
	}
}