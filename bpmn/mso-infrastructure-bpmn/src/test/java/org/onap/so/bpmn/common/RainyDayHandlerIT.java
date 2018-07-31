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

package org.onap.so.bpmn.common;

import static org.onap.so.bpmn.mock.StubResponsePolicy.MockPolicyAbort;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.onap.so.BaseIntegrationTest;

/**
 * Unit test for RainyDayHandler.bpmn.
 */

public class RainyDayHandlerIT extends BaseIntegrationTest {
	
	@Test	
	
	public void  TestRainyDayHandlingSuccess() {

		Map<String, Object> variables = new HashMap<>();
		variables.put("isDebugLogEnabled","true");
		variables.put("msoRequestId", "testRequestId");
		variables.put("serviceType", "X");
		variables.put("vnfType", "Y");
		variables.put("currentActivity", "BB1");		
		variables.put("workStep", "1");
		variables.put("failedActivity", "");
		variables.put("errorCode", "123");
		variables.put("errorText", "update failed");
		variables.put("vnfName", "vSAMP1");
		
		MockPolicyAbort();
		
		
		String businessKey = UUID.randomUUID().toString();
		invokeSubProcess("RainyDayHandler", businessKey, variables);

		waitForProcessEnd(businessKey, 10000);

		Assert.assertTrue(isProcessEnded(businessKey));
		
	}

	
	
}