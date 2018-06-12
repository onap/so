package org.openecomp.mso.client.restproperties;

import static org.junit.Assert.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;
import org.openecomp.mso.BaseTest;

public class PolicyRestPropertiesImplTest extends BaseTest {
	private PolicyRestPropertiesImpl policyRestPropertiesImpl = new PolicyRestPropertiesImpl();
	
	@Test
	public void getEndpointTest() throws MalformedURLException {
		assertEquals(new URL("https://mtanjvsgcvm02.nvp.cip.att.com:8081/pdp/api/"), policyRestPropertiesImpl.getEndpoint());
	}
	
	@Test
	public void getSystemNameTest() throws MalformedURLException {
		assertEquals("MSO", policyRestPropertiesImpl.getSystemName());
	}
	
	@Test
	public void getEnvironmentTest() {
		assertEquals("TEST", policyRestPropertiesImpl.getEnvironment());
	}
	
	@Test
	public void getClientAuthTest() {
		assertEquals("Basic bTAzNzQzOnBvbGljeVIwY2sk", policyRestPropertiesImpl.getClientAuth());
	}
	
	@Test
	public void getAuthTest() {
		assertEquals("Basic dGVzdHBkcDphbHBoYTEyMw==", policyRestPropertiesImpl.getAuth());
	}
}
