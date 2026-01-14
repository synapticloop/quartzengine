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
import synapticloop.quartzengine.engine.QuartzEngine;

import java.lang.reflect.Method;

public class MethodInvokerJob implements Job {
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			JobDataMap dataMap = context.getMergedJobDataMap();
			Object targetObject = dataMap.get(QuartzEngine.TARGET_OBJECT);
			Method method = (Method) dataMap.get(QuartzEngine.TARGET_OBJECT);

			// Retrieve the String array
//			String[] params = (String[]) dataMap.get(QuartzEngine.PARAMS_ARRAY);

			// Logic to invoke the method...
			// If your method takes the String[] as an argument, pass it here
			method.invoke(targetObject);

		} catch (Exception e) {
			throw new JobExecutionException(e);
		}
	}
}