package org.openecomp.mso.client.policy;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openecomp.mso.client.defaultproperties.PolicyRestPropertiesImpl;
import org.openecomp.mso.client.policy.entities.Bbid;
import org.openecomp.mso.client.policy.entities.DictionaryData;
import org.openecomp.mso.client.policy.entities.PolicyDecision;
import org.openecomp.mso.client.policy.entities.PolicyServiceType;
import org.openecomp.mso.client.policy.entities.Workstep;

public class PolicyClientImplTest {
	
	@BeforeClass
	public static void setUp() {
		System.setProperty("mso.config.path", "src/test/resources");
	}
	@Test
	public void successReadProperties() {
		PolicyRestClient client = new PolicyRestClient(new PolicyRestPropertiesImpl(), PolicyServiceType.GET_DECISION);
		client.initializeHeaderMap(client.headerMap);
		
		assertEquals("Found expected Client Auth", client.headerMap.get("ClientAuth"), "Basic bTAzNzQzOnBvbGljeVIwY2sk");
		assertEquals("Found expected Authorization", client.headerMap.get("Authorization"), "Basic dGVzdHBkcDphbHBoYTEyMw==");
		assertEquals("Found expected Environment", client.headerMap.get("Environment"), "TEST");
		assertEquals("Has X-ECOMP-RequestID", client.headerMap.containsKey("X-ECOMP-RequestID"), true);
	}
	
	@Test
	public void getDecisionTest() {
		PolicyClient client = new PolicyClientImpl();
		PolicyDecision decision = client.getDecision("S", "V", "BB1", "1", "123");
		assertEquals("Decision is correct", decision.getDecision(), "PERMIT");
		assertEquals("Decision details is correct", decision.getDetails(), "Retry");
	}
	
	@Test
	public void getAllowedTreatmentsTest(){
		PolicyClient client = new PolicyClientImpl();
		DictionaryData dictClient = client.getAllowedTreatments("BB1", "1");
		final String dictBbidString = dictClient.getBbid().getString();
		final String dictWorkStepString = dictClient.getWorkstep().getString();
		assertEquals("DictionaryData matches a response Bbid", dictBbidString, "BB1");
		assertEquals("DicitonaryData matches a response WorkStep", dictWorkStepString, "1");
	}
	/*
	@Test
	public void getAllowedTreatmentsTest() {
		PolicyClient client = new PolicyClientImpl();
		AllowedTreatments allowedTreatments = client.getAllowedTreatments("BB1", "1");
		int expectedSizeOfList = 4;
		int sizeOfList = allowedTreatments.getAllowedTreatments().size();
		assertEquals("Decision is correct", sizeOfList, expectedSizeOfList);
	}*/
}
