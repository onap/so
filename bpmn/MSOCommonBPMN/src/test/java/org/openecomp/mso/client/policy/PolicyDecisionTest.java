package org.openecomp.mso.client.policy;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class PolicyDecisionTest {
	
	@BeforeClass
	public static void setUp() {
		System.setProperty("mso.config.path", "src/test/resources");
	}
	@Test
	public void successReadProperties() throws IOException, Exception {
		PolicyRestClient client = new PolicyRestClient();
		Map<String, String> headerMap = client.headerMap;
		
		assertEquals("Found expected Client Auth", headerMap.get("ClientAuth"), "Basic bTAzNzQzOnBvbGljeVIwY2sk");
		assertEquals("Found expected Authorization", headerMap.get("Authorization"), "Basic dGVzdHBkcDphbHBoYTEyMw==");
		assertEquals("Found expected Environment", headerMap.get("Environment"), "TEST");
		assertEquals("Has X-ECOMP-RequestID", headerMap.containsKey("X-ECOMP-RequestID"), true);
	}
	
	@Test
	@Ignore // IGNORED FOR 1710 MERGE TO ONAP
	public void restEndpointCall() throws IOException, Exception {
		PolicyRestClient client = new PolicyRestClient();
		PolicyDecision decision = client.getDecision("X", "Y", "BB1", "1", "123");
		assertEquals("Decision is correct", decision.getDecision(), "PERMIT");
		assertEquals("Decision details is correct", decision.getDetails(), "Abort");
	}
/*
	@Test
	public void failure() throws IOException, Exception {
		when(dmaapConsumer.getMRConsumer()).thenReturn(mrConsumer);
		when(mrConsumer.fetch())
				.thenReturn(Arrays.asList(new String[] { getJson("response.json"), getJson("output-failure.json") }));

		SDNOValidatorImpl validator = new SDNOValidatorImpl();
		thrown.expect(SDNOException.class);
		thrown.expectMessage("my message");
		boolean result = validator.pollForResponse(dmaapConsumer, "xyz123");

	}*/
}
