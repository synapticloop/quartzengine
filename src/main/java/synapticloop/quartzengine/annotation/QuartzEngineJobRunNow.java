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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>A marker annotation that indicates a {@link QuartzEngineJob} should be executed
 * immediately upon application startup.</p>
 *
 * <p>When the {@code QuartzEngine} discovers this annotation on a method, it will
 * schedule the job normally according to its Cron expression, but will also
 * issue an immediate manual trigger call.</p>
 *
 * <p>Note: This must be used in conjunction with {@literal @}QuartzJob to have any effect.</p>
 * <pre>
 * &#64;QuartzJobRunNow
 * &#64;QuartzJob(cronExpression = "0 0 12 * * ?")
 * public void dailyReport() { ... }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface QuartzEngineJobRunNow {
}