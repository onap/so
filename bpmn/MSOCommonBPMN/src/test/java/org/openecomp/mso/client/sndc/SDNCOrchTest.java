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

package org.openecomp.mso.client.sndc;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openecomp.mso.bpmn.core.domain.Customer;
import org.openecomp.mso.bpmn.core.domain.ModelInfo;
import org.openecomp.mso.bpmn.core.domain.Request;
import org.openecomp.mso.bpmn.core.domain.ServiceDecomposition;
import org.openecomp.mso.bpmn.core.domain.ServiceInstance;
import org.openecomp.mso.bpmn.core.json.JsonDecomposingException;
import org.openecomp.mso.client.orchestration.SDNCOrchestrator;
import org.openecomp.mso.client.sdnc.sync.SDNCSyncRpcClient;

public class SDNCOrchTest {
	
	@BeforeClass
	public static void setup() {
		System.setProperty("mso.config.path", "src/test/resources/");
	}
	
	@Test
	public void run () throws JsonDecomposingException {
		ServiceDecomposition serviceDecomp = new ServiceDecomposition ("{\"serviceResources\":{}}","123");
		
		ServiceInstance servInst = new ServiceInstance();
		servInst.setInstanceId("RaaTest-1-id");
		servInst.setServiceType("");
		servInst.setInstanceName("some-junk-name");
		servInst.setServiceId("a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb");
		
		ModelInfo model = new ModelInfo();
		model.setModelInvariantUuid("uuid-miu-svc-011-abcdef");
		model.setModelName("SIModelName1");
		model.setModelUuid("");
		model.setModelVersion("2");
		
		Request req = new Request();
		req.setRequestId("RaaCSIRequestId-1");
		req.setSdncRequestId("c0c5e8bf-d5c4-4d81-b2a9-78832c2c789e");
		req.setModelInfo(model);
		
		Customer cust = new Customer();
		cust.setGlobalSubscriberId("MCBH-1610");
		cust.setSubscriptionServiceType("viprsvc");
		
		serviceDecomp.setCustomer(cust);
		serviceDecomp.setRequest(req);
		serviceDecomp.setCallbackURN("http://localhost:28080/mso/SDNCAdapterCallbackService");
		serviceDecomp.setServiceInstance(servInst);
		
		SDNCOrchestrator sdncO = new SDNCOrchestrator();
		sdncO.createServiceInstance(serviceDecomp);
	}
	
	@Test
	public void runValidateSdncResponse () {
		SDNCSyncRpcClient client = new SDNCSyncRpcClient(null, null);
		client.validateSDNCResponse("{\"v1:RequestData\": {\"output\": {\"svc-request-id\": \"0ca5bf8f-c944-4318-810b-6ddfbec13cc5\",\"response-code\": \"200\",\"response-message\": \"a\"}}}");
	}
}
