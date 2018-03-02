package org.openecomp.mso.apihandlerinfra.tenantisolation.dmaap;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openecomp.mso.apihandlerinfra.tenantisolation.dmaap.OperationalEnvironmentPublisher;
import org.openecomp.mso.properties.MsoPropertiesException;
import org.openecomp.mso.properties.MsoPropertiesFactory;

public class OperationalEnvironmentPublisherTest {

	
	@BeforeClass
	public static void setUp() throws MsoPropertiesException {
		System.setProperty("mso.config.path", "src/test/resources/");
		MsoPropertiesFactory propertiesFactory = new MsoPropertiesFactory();
		propertiesFactory.initializeMsoProperties("MSO_PROP_APIHANDLER_INFRA", "mso.apihandler-infra.properties");
	}
	
	@Test
	public void getProperties() throws FileNotFoundException, IOException {
		OperationalEnvironmentPublisher publisher = new OperationalEnvironmentPublisher();
		
		assertEquals("m97898@mso.ecomp.att.com", publisher.getUserName());
		assertEquals("VjR5NDcxSzA=", publisher.getPassword());
		assertEquals("com.att.ecomp.mso.operationalEnvironmentEvent", publisher.getTopic());
		assertEquals("https://dcae-mrtr-ftl3.ecomp.cci.att.com:3905", publisher.getHost().get());
	}
}
