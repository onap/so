/*
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
package org.openecomp.mso.bpmn.vcpe.workflow;

import javax.ws.rs.Path;

import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.ProcessEngines;
import org.openecomp.mso.bpmn.common.workflow.service.WorkflowAsyncResource;


/**
 * 
 * @version 1.0
 * Asynchronous Workflow processing using JAX RS RESTeasy implementation
 * Both Synchronous and Asynchronous BPMN process can benefit from this implementation since the workflow gets executed in the background
 * and the server thread is freed up, server scales better to process more incoming requests
 * 
 * Usage: For synchronous process, when you are ready to send the response invoke the callback to write the response
 * For asynchronous process - the activity may send a acknowledgement response and then proceed further on executing the process
 */
@Path("/async")
public class WorkflowAsyncVcpeResource extends WorkflowAsyncResource {
	
	protected ProcessEngineServices getProcessEngineServices() {
        return pes4junit.orElse(ProcessEngines.getProcessEngine("vcpe"));
	}
}
