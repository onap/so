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

package org.openecomp.mso.bpmn.infrastructure.scripts

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.apache.commons.lang3.*

public class HealthCheckActivate extends AbstractServiceTaskProcessor {
	
	
		/**
	 * Validates the request message and sets up the workflow.
	 * @param execution the execution
	 */
	public void preProcessRequest(Execution execution) {

	}
	
	
	/**
	 * Sends the synchronous response back to the API Handler.
	 * @param execution the execution
	 */
	public void sendResponse(Execution execution) {
		def status = execution.getVariable("healthyStatus")
		def healthcheckmessage = execution.getVariable("healthcheckmessage")
			if (status == "true") {
				sendWorkflowResponse(execution, 200, healthcheckmessage)
			}else{
				sendWorkflowResponse(execution, 503, healthcheckmessage)
			}
	}

}
