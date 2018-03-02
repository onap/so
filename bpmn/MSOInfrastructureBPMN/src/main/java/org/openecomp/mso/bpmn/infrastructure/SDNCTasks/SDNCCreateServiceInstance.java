/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.mso.bpmn.infrastructure.SDNCTasks;

import java.util.logging.Logger;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil;
import org.openecomp.mso.bpmn.core.domain.ServiceDecomposition;
import org.openecomp.mso.client.orchestration.SDNCOrchestrator;

public class SDNCCreateServiceInstance implements JavaDelegate {
	private static Logger LOGGER = Logger.getLogger("SDNCCreateServiceInstance");
	SDNCOrchestrator sdncO = new SDNCOrchestrator();
	ExceptionUtil exceptionUtil = new ExceptionUtil();

	public void execute(DelegateExecution execution) throws Exception {
		LOGGER.info("**** Started SDNCCreateServiceInstance ****");
		ServiceDecomposition serviceDecomp = (ServiceDecomposition) execution.getVariable("serviceDecomposition");
		if (serviceDecomp != null) {
			try {
				sdncO.createServiceInstance(serviceDecomp);
			} catch (Exception ex) {
				String msg = "Exception in sdncCreateServiceInstance. " + ex.getMessage();
				exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg);
			}
		}
		LOGGER.info("**** Finished SDNCCreateServiceInstance ****");
	}
}
