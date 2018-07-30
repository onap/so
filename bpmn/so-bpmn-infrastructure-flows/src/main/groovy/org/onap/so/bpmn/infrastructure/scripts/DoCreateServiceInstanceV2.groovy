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

package org.onap.so.bpmn.infrastructure.scripts;

import static org.apache.commons.lang3.StringUtils.*;

import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.client.orchestration.AAIServiceInstanceResources
import org.onap.so.client.orchestration.SDNCServiceInstanceResources
import org.onap.so.logger.MsoLogger
import org.onap.so.logger.MessageEnum

import groovy.json.*

/**
 * This groovy class supports the <class>DoCreateServiceInstanceV2.bpmn</class> process.
 *
*/

public class DoCreateServiceInstanceV2 extends AbstractServiceTaskProcessor {
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, DoCreateServiceInstanceV2.class);
	AAIServiceInstanceResources aaiO = new AAIServiceInstanceResources()
	SDNCServiceInstanceResources sdncO = new SDNCServiceInstanceResources()
	
	@Override
	public void preProcessRequest(DelegateExecution execution) {
	}

	public void createServiceInstance(DelegateExecution execution) { 
		execution.setVariable("callSDNC",true)
		if(execution.getVariable("serviceType").equalsIgnoreCase("PORT-MIRROR")== false){
				if(execution.getVariable("sdncVersion").equals("1610")){
					execution.setVariable("callSDNC",false);				
				}
		}
		ServiceDecomposition serviceDecomp = (ServiceDecomposition) execution.getVariable("serviceDecomposition")
		try{
			aaiO.createServiceInstance(serviceDecomp)
		} catch (BpmnError e) {
			throw e
		}
	}
	
	public void createProject(DelegateExecution execution) {
		ServiceDecomposition serviceDecomp = (ServiceDecomposition) execution.getVariable("serviceDecomposition")
		if (serviceDecomp.getServiceInstance() != null && serviceDecomp.getProject() != null) {	
			try{
				aaiO.createProjectandConnectServiceInstance(serviceDecomp)
			} catch (BpmnError e) {
				throw e
			}
		}
	}
	
	public void createOwningEntity(DelegateExecution execution) {
		ServiceDecomposition serviceDecomp = (ServiceDecomposition) execution.getVariable("serviceDecomposition")
		if (serviceDecomp.getServiceInstance() != null && serviceDecomp.getOwningEntity() != null) {
			try{
				aaiO.createOwningEntityandConnectServiceInstance(serviceDecomp)
			} catch (BpmnError e) {
				throw e
			}	
		}			
	}
	
	public void sdncAssignRequest(DelegateExecution execution) {
		ServiceDecomposition serviceDecomp = (ServiceDecomposition) execution.getVariable("serviceDecomposition")
		if (serviceDecomp != null) {
			try {
				sdncO.sendSyncResponse(serviceDecomp)
			} catch (BpmnError e) {
				throw e
			}
		}

	}
	
	public void rollback(DelegateExecution execution) {
		//TODO
	}
	
}
