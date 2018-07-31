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

package org.onap.so.bpmn.infrastructure.aai;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.onap.so.bpmn.common.scripts.ExceptionUtil;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.aai.AAIResourcesClient;
import org.onap.so.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.client.aai.entities.uri.AAIUriFactory;
import org.springframework.stereotype.Component;

public class AAIDeleteServiceInstance extends AAIResource implements JavaDelegate{

	ExceptionUtil exceptionUtil = new ExceptionUtil();
	public void execute(DelegateExecution execution) throws Exception {
		try{
			String serviceInstanceId = (String) execution.getVariable("serviceInstanceId");
			AAIResourceUri serviceInstanceURI = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE,
					serviceInstanceId);
			getAaiClient().delete(serviceInstanceURI);
			execution.setVariable("GENDS_SuccessIndicator",true);
		} catch(Exception ex){
			String msg = "Exception in Delete Serivce Instance. Service Instance could not be deleted in AAI." + ex.getMessage();
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg);
		}
		
	}
	
}
