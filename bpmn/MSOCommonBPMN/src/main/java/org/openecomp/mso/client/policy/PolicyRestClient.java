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

package org.openecomp.mso.client.policy;

import java.util.Map;
import java.util.UUID;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Service;

@Service
public class PolicyRestClient extends RestClient {

	private static final String ENDPOINT_KEY = "policy.endpoint";
	private static final String X_ECOMP_REQUESTID = String.valueOf(UUID.randomUUID());

	public PolicyRestClient() {
		super(ENDPOINT_KEY);
	}

	@Override
	protected void initializeHeaderMap(Map<String, String> headerMap) {
		headerMap.put("ClientAuth", properties.get("policy.client.auth"));
		headerMap.put("Authorization", properties.get("policy.auth"));
		headerMap.put("Environment", properties.get("policy.environment"));
		headerMap.put("X-ECOMP-RequestID", X_ECOMP_REQUESTID);
	}

	public PolicyDecision getDecision(String serviceType, String vnfType, String bbID, String workStep,
			String errorCode) {
		DecisionAttributes decisionAttributes = new DecisionAttributes();
		decisionAttributes.setServiceType(serviceType);
		decisionAttributes.setVNFType(vnfType);
		decisionAttributes.setBBID(bbID);
		decisionAttributes.setWorkStep(workStep);
		decisionAttributes.setErrorCode(errorCode);

		return this.getDecision(decisionAttributes);
	}

	private PolicyDecision getDecision(DecisionAttributes decisionAttributes) {
		PolicyDecisionRequest decisionRequest = new PolicyDecisionRequest();
		decisionRequest.setDecisionAttributes(decisionAttributes);
		decisionRequest.setEcompcomponentName(ECOMP_COMPONENT_NAME);

		return this.getBuilder().accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.entity(decisionRequest, MediaType.APPLICATION_JSON)).readEntity(PolicyDecision.class);
	}
}