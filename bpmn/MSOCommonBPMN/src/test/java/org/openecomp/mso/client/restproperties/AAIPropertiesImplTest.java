package org.openecomp.mso.client.restproperties;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;
import org.openecomp.mso.BaseTest;
import org.openecomp.mso.client.aai.AAIVersion;

public class AAIPropertiesImplTest extends BaseTest {

	private AAIPropertiesImpl aaiPropertiesImpl = new AAIPropertiesImpl();
	
	@Test
	public void getEndpointTest() throws MalformedURLException {
		URL expectedEndpoint = new URL("http://localhost:28090");
		assertEquals(expectedEndpoint, aaiPropertiesImpl.getEndpoint());
	}
	
	@Test
	public void getSystemNameTest() {
		assertEquals("MSO", aaiPropertiesImpl.getSystemName());
	}
	
	@Test
	public void getDefaultVersionTest() {
		assertEquals(AAIVersion.LATEST, aaiPropertiesImpl.getDefaultVersion());
	}
	
	@Test
	public void getAuthTest() {
		String expectedAuth = "26AFB797A6A57960D5D718491925C50F77CDC22AC394B3DBA09950D8FD1C0764";
		assertEquals(expectedAuth, aaiPropertiesImpl.getAuth());
	}
	
	@Test
	public void getKeyTest() {
		String expectedKey = "07a7159d3bf51a0e53be7a8f89699be7";
		assertEquals(expectedKey, aaiPropertiesImpl.getKey());
	}
}
