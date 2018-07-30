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

package org.onap.so.bpmn.infrastructure.scripts

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.runtime.Execution
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.client.orchestration.AAIServiceInstanceResources
import org.onap.so.logger.MsoLogger


public class DoCreateServiceInstanceRollbackV2 extends AbstractServiceTaskProcessor{
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, DoCreateServiceInstanceRollbackV2.class);
	
	@Override
	public void preProcessRequest(DelegateExecution execution) {
		
	}
	
	public void aaiServiceInstanceRollback (DelegateExecution execution) {
		def aaiServiceInstanceRollback = execution.getVariable("aaiServiceInstanceRollback")
		if(aaiServiceInstanceRollback){
			msoLogger.trace("Started aaiServiceInstanceRollback")
			try{
				ServiceDecomposition serviceDecomp = execution.getVariable("ServiceDecomposition")
				AAIServiceInstanceResources aaiO = new AAIServiceInstanceResources()
				aaiO.deleteServiceInstance(serviceDecomp)
			}catch (Exception ex) {
				String msg =  "Error Response from AAI for aaiServiceInstanceRollback"
				execution.setVariable("rollbackError", msg)
				msoLogger.debug(msg)
				throw new BpmnError("MSOWorkflowException")
			}
			msoLogger.trace("Completed aaiServiceInstanceRollback")
		}else{
			msoLogger.trace("SKIPPING A&AI ROLLBACK")
		}
	}
	
	public void rollbackError (DelegateExecution execution) {
		msoLogger.trace("rollbackError")
		try{
			msoLogger.debug("Caught an Exception in DoCreateServiceInstanceRollbackV2")
		}catch(BpmnError b){
			msoLogger.debug("BPMN Error during rollbackError: " + b.getMessage())
		}catch(Exception e){
			msoLogger.debug("Caught Exception during rollbackError: " + e.getMessage())
		}
		msoLogger.debug(" Exit processRollbackException")
	}
	
}
