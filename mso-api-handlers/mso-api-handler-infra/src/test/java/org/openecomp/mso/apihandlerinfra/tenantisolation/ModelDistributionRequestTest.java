package org.openecomp.mso.apihandlerinfra.tenantisolation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.mockito.Mockito;

public class ModelDistributionRequestTest {

	private static final String requestJSON = "{\"status\": \"DISTRIBUTION_COMPLETE_ERROR\", \"errorReason\": \"Distribution failed in AAI\" }";
	
	@Test
	public void testUpdateModelDistributionStatus() {
		final Response okResponse = Response.status(HttpStatus.SC_OK).build();

		try {
			ModelDistributionRequest mdr = Mockito.mock(ModelDistributionRequest.class);
			Mockito.when(mdr.updateModelDistributionStatus(requestJSON, "v1", "ff3514e3-5a33-55df-13ab-12abad84e7ff")).thenReturn(okResponse);
			Response resp = mdr.updateModelDistributionStatus(requestJSON, "v1", "ff3514e3-5a33-55df-13ab-12abad84e7ff");
			assertEquals(resp.getStatus(), HttpStatus.SC_OK);
		} catch (Exception e) {
			fail("Exception caught: " + e.getMessage());
		}
	}
	
	@Test
	public void testObjectMapperError() {
		ModelDistributionRequest request = new ModelDistributionRequest();
		Response response = request.updateModelDistributionStatus(null, null, null);
		String body = response.getEntity().toString();
		assertTrue(body.contains("Mapping of request to JSON object failed."));
		assertEquals(400, response.getStatus());
	}
	
	@Test
	public void testParseError1() {
		String requestErrorJSON = "{\"errorReason\": \"Distribution failed in AAI\" }";

		ModelDistributionRequest request = new ModelDistributionRequest();
		Response response = request.updateModelDistributionStatus(requestErrorJSON, null, null);
		String body = response.getEntity().toString();
		assertTrue(body.contains("Error parsing request."));
		assertEquals(400, response.getStatus());
	}
	
	@Test
	public void testParseError2() {
		String requestErrorJSON = "{\"status\": \"DISTRIBUTION_COMPLETE_ERROR\"}";

		ModelDistributionRequest request = new ModelDistributionRequest();
		Response response = request.updateModelDistributionStatus(requestErrorJSON, null, null);
		String body = response.getEntity().toString();
		assertTrue(body.contains("Error parsing request."));
		assertEquals(400, response.getStatus());
	}
	
	@Test
	public void testSuccess() {
		ModelDistributionRequest request = new ModelDistributionRequest();
		TenantIsolationRunnable thread = mock(TenantIsolationRunnable.class);
		request.setThread(thread);
		
		Response response = request.updateModelDistributionStatus(requestJSON, null, null);
		
		assertEquals(200, response.getStatus());
	}
}
