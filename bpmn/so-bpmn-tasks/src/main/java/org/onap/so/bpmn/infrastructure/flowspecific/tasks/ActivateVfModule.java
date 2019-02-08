/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.infrastructure.flowspecific.tasks;

import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.client.exception.ExceptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ActivateVfModule {

    private static final Logger logger = LoggerFactory.getLogger(ActivateVfModule.class);
	
	protected static final String VF_MODULE_TIMER_DURATION_PATH = "mso.workflow.vfModuleActivate.timer.duration";
	protected static final String DEFAULT_TIMER_DURATION = "PT180S";
	
	@Autowired
	private ExceptionBuilder exceptionUtil;
	
	@Autowired
    private Environment environment;


	public void setTimerDuration(BuildingBlockExecution execution) {
		try {
			String waitDuration = this.environment.getProperty(VF_MODULE_TIMER_DURATION_PATH, DEFAULT_TIMER_DURATION);
			logger.debug("Sleeping before proceeding with SDNC activate. Timer duration: {}", waitDuration);
			execution.setVariable("vfModuleActivateTimerDuration", waitDuration);
		} catch (Exception e) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, e);
		}
	}
}
