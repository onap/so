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

package org.onap.so.apihandlerinfra.tenantisolation.helpers;

import org.onap.aai.domain.yang.OperationalEnvironment;
import org.onap.so.apihandlerinfra.tenantisolation.CloudOrchestrationRequest;
import org.springframework.stereotype.Component;

@Component
public class AAIClientObjectBuilder {
	
	public OperationalEnvironment buildAAIOperationalEnvironment(String status, CloudOrchestrationRequest cloudOrchestrationRequest) {
		OperationalEnvironment env = new OperationalEnvironment();
		env.setOperationalEnvironmentId(cloudOrchestrationRequest.getOperationalEnvironmentId());
		env.setOperationalEnvironmentName(cloudOrchestrationRequest.getRequestDetails().getRequestInfo().getInstanceName());
		env.setOperationalEnvironmentType(cloudOrchestrationRequest.getRequestDetails().getRequestParameters().getOperationalEnvironmentType().toString());
		env.setOperationalEnvironmentStatus(status);
		env.setTenantContext(cloudOrchestrationRequest.getRequestDetails().getRequestParameters().getTenantContext());
		env.setWorkloadContext(cloudOrchestrationRequest.getRequestDetails().getRequestParameters().getWorkloadContext());
		return env;	
	}


}
