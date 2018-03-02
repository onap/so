package org.openecomp.mso.asdc.tenantIsolation;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openecomp.mso.properties.MsoJavaProperties;
import org.openecomp.mso.properties.MsoPropertiesFactory;

import java.net.URL;

public class AaiClientPropertiesImplTest {

	@BeforeClass
	public static void setup() throws Exception {
		System.setProperty("mso.config.path", "src/test/resources");
	}
	
	
	@Test
	public void testGetEndpoint() throws Exception {

		MsoPropertiesFactory msoPropertiesFactory = new MsoPropertiesFactory();
		msoPropertiesFactory.initializeMsoProperties(AsdcPropertiesUtils.MSO_ASDC_CLIENTS, "/mso.asdc.clients.properties");

		AaiClientPropertiesImpl aaiPropertiesImpl = new AaiClientPropertiesImpl();		
		String aaiEndpoint = aaiPropertiesImpl.getEndpoint().toString();
		
		assertEquals("AAI endpoint", "http://localhost:28090", aaiEndpoint);
		
	}
	
}
