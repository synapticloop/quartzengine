package synapticloop.quartzengine.annotation;

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

import synapticloop.quartzengine.engine.QuartzEngine;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Used to mark a method for automatic scheduling via the {@link QuartzEngine}.</p>
 * * <p>The method signature must either be empty or accept a single
 * {@link org.quartz.JobExecutionContext} parameter.</p>
 *
 * <pre>
 *  &#64;QuartzJob(cronExpression = "0 0/10 * * * ?", parameters = {"Prod", "v1"})
 *  public void myScheduledTask() { ... }
 * </pre>
 *
 * @author synapticloop
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface QuartzEngineJob {
	String UNDEFINED = "undefined";

	/**
	 * <p>The Quartz Cron expression determining when the job fires. Quartz cron
	 * expressions consist of 6 or 7 fields (Seconds, Minutes, Hours,
	 * Day-of-Month, Month, Day-of-Week, Year).</p>
	 *
	 * @return a valid cron string
	 */
	String cronExpression();

	/**
	 * <p>The logical group name for this job. Groups allow you to categorize
	 * jobs for bulk operations like pausing or resuming all jobs in a group.</p>
	 *
	 * @return the group name, defaults to "undefined"
	 */
	String group() default UNDEFINED;

	/**
	 * <p>A collection of static parameters passed to the job's {@code JobDataMap}.
	 * These can be retrieved during execution via
	 * {@code context.getMergedJobDataMap()}.</p>
	 *
	 * @return an array of strings
	 */
	String[] parameters() default {}; // Changed from String to String[]
}