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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.mso.client.defaultproperties.PolicyRestPropertiesImpl;
import org.openecomp.mso.client.policy.entities.DecisionAttributes;
import org.openecomp.mso.client.policy.entities.DictionaryData;
import org.openecomp.mso.client.policy.entities.PolicyDecision;
import org.openecomp.mso.client.policy.entities.PolicyServiceType;

public class PolicyClientImplTest {

	@Test
	public void successReadProperties() {
		PolicyRestClient client = new PolicyRestClient(new PolicyRestPropertiesImpl(), PolicyServiceType.GET_DECISION);
		client.initializeHeaderMap(client.headerMap);

		assertEquals("Found expected Client Auth", client.headerMap.get("ClientAuth"),
				"Basic bTAzNzQzOnBvbGljeVIwY2sk");
		assertEquals("Found expected Authorization", client.headerMap.get("Authorization"),
				"Basic dGVzdHBkcDphbHBoYTEyMw==");
		assertEquals("Found expected Environment", client.headerMap.get("Environment"), "TEST");
		assertEquals("Has X-ECOMP-RequestID", client.headerMap.containsKey("X-ECOMP-RequestID"), true);
	}

	@Test
	public void getDecisionMockTest() {
		String serviceType = "S";
		String vnfType = "V";
		String bbID = "BB1";
		String workStep = "1";
		String errorCode = "123";
		
		PolicyDecision expected = new PolicyDecision();
		expected.setDecision("PERMIT");
		expected.setDetails("Retry");
		
		DecisionAttributes decisionAttributes = new DecisionAttributes();
		decisionAttributes.setServiceType(serviceType);
		decisionAttributes.setVNFType(vnfType);
		decisionAttributes.setBBID(bbID);
		decisionAttributes.setWorkStep(workStep);
		decisionAttributes.setErrorCode(errorCode);
		PolicyClient client = Mockito.spy(PolicyClientImpl.class);
		
		doReturn(expected).when(client).getDecision(serviceType, vnfType, bbID, workStep, errorCode);

		PolicyDecision actual = client.getDecision(serviceType, vnfType, bbID, workStep, errorCode);
		assertThat(actual, sameBeanAs(expected));
	}

	@Test
	@Ignore
	public void getDecisionTest() {
		PolicyClient client = new PolicyClientImpl();
		PolicyDecision decision = client.getDecision("S", "V", "BB1", "1", "123");
		assertEquals("Decision is correct", decision.getDecision(), "PERMIT");
		assertEquals("Decision details is correct", decision.getDetails(), "Retry");
	}

	@Test
	@Ignore
	public void getAllowedTreatmentsTest() {
		PolicyClient client = new PolicyClientImpl();
		DictionaryData dictClient = client.getAllowedTreatments("BB1", "1");
		final String dictBbidString = dictClient.getBbid().getString();
		final String dictWorkStepString = dictClient.getWorkstep().getString();
		assertEquals("DictionaryData matches a response Bbid", dictBbidString, "BB1");
		assertEquals("DicitonaryData matches a response WorkStep", dictWorkStepString, "1");
	}
	/*
	 * @Test public void getAllowedTreatmentsTest() { PolicyClient client = new
	 * PolicyClientImpl(); AllowedTreatments allowedTreatments =
	 * client.getAllowedTreatments("BB1", "1"); int expectedSizeOfList = 4; int
	 * sizeOfList = allowedTreatments.getAllowedTreatments().size();
	 * assertEquals("Decision is correct", sizeOfList, expectedSizeOfList); }
	 */
}
