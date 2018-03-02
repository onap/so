package org.openecomp.mso.client.aai.entities.uri;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.UriBuilder;

import org.junit.Test;
import org.openecomp.mso.client.aai.AAIObjectType;

public class SimpleUriFromUriTest {

	
	
	@Test
	public void removeHost() {
		
		AAIUri uri = new SimpleUri(AAIObjectType.UNKNOWN, UriBuilder.fromUri("https://aai-conexus-e2e.test.att.com:8443/aai/v9/network/vces/vce/a9fec18e-1ea3-40e4-a6c0-a89b3de07053").build());
		
		assertEquals("root and version removed", "/network/vces/vce/a9fec18e-1ea3-40e4-a6c0-a89b3de07053", uri.build().toString());
		
	}
	@Test
	public void noChange() {
		
		AAIUri uri = new SimpleUri(AAIObjectType.UNKNOWN, UriBuilder.fromUri("/network/vces/vce/a9fec18e-1ea3-40e4-a6c0-a89b3de07053").build());
		
		assertEquals("no change", "/network/vces/vce/a9fec18e-1ea3-40e4-a6c0-a89b3de07053", uri.build().toString());
		
	}
	
	@Test
	public void encodingPreserved() {
		
		AAIUri uri = new SimpleUri(AAIObjectType.UNKNOWN, UriBuilder.fromUri("/network/vces/vce/a9f%20%20ec18e-1ea3-40e4-a6c0-a89b3de07053").build());
		
		assertEquals("encoding preserved", "/network/vces/vce/a9f%20%20ec18e-1ea3-40e4-a6c0-a89b3de07053", uri.build().toString());
		
	}
}
