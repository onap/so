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

package org.openecomp.mso.apihandlerinfra.tenantisolation.helpers;

import org.openecomp.mso.apihandlerinfra.tenantisolation.CloudOrchestrationRequest;
import org.openecomp.mso.client.aai.objects.AAIOperationalEnvironment;

public class AAIClientObjectBuilder {
	
	private CloudOrchestrationRequest cloudOrchestrationRequest;
	
    public AAIClientObjectBuilder(CloudOrchestrationRequest request) {
    	this.cloudOrchestrationRequest = request;
	}

	/**
     * Create an AAIOperationalEnvironment object.
     * @param environmentId
     * @param environmentName
     * @param environmentType
     * @param envrionmentStatus
     * @param tenantContext
     * @param workloadContext
     * @return
     * @throws JsonProcessingException
     */
    @Deprecated
	public static AAIOperationalEnvironment createAAIOperationalEnvironment(
			String environmentId,
			String environmentName,
			String environmentType,
			String envrionmentStatus,
			String tenantContext,
			String workloadContext) {
		
		AAIOperationalEnvironment oe = new AAIOperationalEnvironment();
		oe.setOperationalEnvironmentId(environmentId);
		oe.setOperationalEnvironmentName(environmentName);
		oe.setOperationalEnvironmentType(environmentType);
		oe.setOperationalEnvironmentStatus(envrionmentStatus);
		oe.setTenantContext(tenantContext);
		oe.setWorkloadContext(workloadContext);
		
		return oe;
	}

    
	public AAIOperationalEnvironment buildAAIOperationalEnvironment(String status) {
		AAIOperationalEnvironment env = new AAIOperationalEnvironment();
		env.setOperationalEnvironmentId(this.cloudOrchestrationRequest.getOperationalEnvironmentId());
		env.setOperationalEnvironmentName(this.cloudOrchestrationRequest.getRequestDetails().getRequestInfo().getInstanceName());
		env.setOperationalEnvironmentType(this.cloudOrchestrationRequest.getRequestDetails().getRequestParameters().getOperationalEnvironmentType().toString());
		env.setOperationalEnvironmentStatus(status);
		env.setTenantContext(this.cloudOrchestrationRequest.getRequestDetails().getRequestParameters().getTenantContext());
		env.setWorkloadContext(this.cloudOrchestrationRequest.getRequestDetails().getRequestParameters().getWorkloadContext());
		return env;	
	}


}
