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

package org.openecomp.mso.bpmn.infrastructure.DoCreateServiceInstance;

import java.util.logging.Logger;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.json.JSONObject;
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil;
import org.openecomp.mso.bpmn.core.domain.Customer;
import org.openecomp.mso.bpmn.core.domain.ModelInfo;
import org.openecomp.mso.bpmn.core.domain.OwningEntity;
import org.openecomp.mso.bpmn.core.domain.Project;
import org.openecomp.mso.bpmn.core.domain.Request;
import org.openecomp.mso.bpmn.core.domain.ServiceDecomposition;
import org.openecomp.mso.bpmn.core.domain.ServiceInstance;

public class SetupServiceDecomp implements JavaDelegate {

	ExceptionUtil exceptionUtil = new ExceptionUtil();
	private static Logger LOGGER = Logger.getLogger("SetupServiceDecomp");

	public void execute(DelegateExecution execution) throws Exception {
		LOGGER.info("Starting SetupServiceDecomp");
		try {
			String json = (String) execution.getVariable("bpmnRequest");
			JSONObject jsonObj = new JSONObject(json);
			JSONObject jsonReq = jsonObj.getJSONObject("requestDetails");
			JSONObject jsonServ = jsonReq.getJSONObject("serviceInstance");
			String serviceInstanceId = jsonServ.getString("serviceInstanceId");
			System.out.println("servInstId: "+serviceInstanceId);
			String serviceInstanceName = jsonServ.getString("serviceInstanceName");
			String serviceType = jsonServ.getString("serviceType");
			String serviceRole = jsonServ.getString("serviceRole");
			String modelInvariantUuid = jsonServ.getString("modelInvariantUuid");
			String modelUuid = jsonServ.getString("modelUuid");
			String modelVersion = jsonServ.getString("modelVersion");
			String modelName = jsonServ.getString("modelName");
			String environmentContext = jsonServ.getString("environmentContext");
			String workloadContext = jsonServ.getString("workloadContext");
			JSONObject jsonProject = jsonReq.getJSONObject("project");
			String projectName = jsonProject.getString("projectName");
			JSONObject jsonOwningEntity = jsonReq.getJSONObject("owningEntity");
			String owningEntityId = jsonOwningEntity.getString("owningEntityId");
			String owningEntityName = jsonOwningEntity.getString("owningEntityName");
			JSONObject jsonCustomer = jsonReq.getJSONObject("customer");
			String subscriptionServiceType = jsonCustomer.getString("subscriptionServiceType");
			String globalSubscriberId = jsonCustomer.getString("globalSubscriberId");
			JSONObject jsonRequest = jsonReq.getJSONObject("request");
			String sdncRequestId = jsonRequest.getString("sdncRequestId");
			String callbackURL = jsonRequest.getString("callbackURL");
			String requestId = jsonRequest.getString("requestId");
			String productFamilyId = jsonRequest.getString("productFamilyId");
			ServiceDecomposition serviceDecomp = new ServiceDecomposition("{}");
			serviceDecomp.setCallbackURN(callbackURL);
			serviceDecomp.setServiceRole(serviceRole);
			ModelInfo modelInfo = new ModelInfo();
			modelInfo.setModelInvariantUuid(modelInvariantUuid);
			modelInfo.setModelName(modelName);
			modelInfo.setModelVersion(modelVersion);
			modelInfo.setModelUuid(modelUuid);
			ServiceInstance serviceInstance = new ServiceInstance();
			serviceInstance.setInstanceId(serviceInstanceId);
			serviceInstance.setInstanceName(serviceInstanceName);
			serviceInstance.setServiceType(serviceType);
			serviceInstance.setModelInfo(modelInfo);
			serviceInstance.setEnvironmentContext(environmentContext);
			serviceInstance.setWorkloadContext(workloadContext);
			Project project = new Project();
			project.setProjectName(projectName);
			OwningEntity owningEntity = new OwningEntity();
			owningEntity.setOwningEntityId(owningEntityId);
			owningEntity.setOwningEntityName(owningEntityName);
			Customer customer = new Customer();
			customer.setGlobalSubscriberId(globalSubscriberId);
			customer.setSubscriptionServiceType(subscriptionServiceType);
			Request request = new Request();
			request.setRequestId(requestId);
			request.setSdncRequestId(sdncRequestId);
			request.setProductFamilyId(productFamilyId);
			serviceDecomp.setCustomer(customer);
			serviceDecomp.setServiceInstance(serviceInstance);
			serviceDecomp.setRequest(request);
			serviceDecomp.setProject(project);
			serviceDecomp.setOwningEntity(owningEntity);
			execution.setVariable("ServiceDecomposition", serviceDecomp);
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, "ERROR IN SET UP SERVICE DECOMP");
		}
		LOGGER.info("Finished SetupServiceDecomp");
	}

}
