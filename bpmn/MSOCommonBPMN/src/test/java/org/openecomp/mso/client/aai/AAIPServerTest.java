package org.openecomp.mso.client.aai;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openecomp.aai.domain.yang.Pserver;
import static org.junit.Assert.assertEquals;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
public class AAIPServerTest {

	@BeforeClass
	public static void setUp() {
		System.setProperty("mso.config.path", "src/test/resources");
		System.setProperty("javax.net.ssl.keyStore", "C:/etc/ecomp/mso/config/msoClientKeyStore.jks");
		System.setProperty("javax.net.ssl.keyStorePassword", "mso4you");
		System.setProperty("javax.net.ssl.trustStore", "C:/etc/ecomp/mso/config/msoTrustStore.jks");
		System.setProperty("javax.net.ssl.trustStorePassword", "mso_Domain2.0_4you");
	}
	
	@Test
	@Ignore // IGNORED FOR 1710 MERGE TO ONAP
	public void pserverTest() throws JsonParseException, JsonMappingException, IOException, NoSuchAlgorithmException {
		AAIRestClientImpl client = new AAIRestClientImpl();
		File file = new File("src/test/resources/__files/AAI/pserver.json");
		List<Pserver> list = client.getListOfPservers(file);
		
		assertEquals("", list.get(0).getHostname(), "test");
	}
	
	@Test
	@Ignore // IGNORED FOR 1710 MERGE TO ONAP
	public void pserverActualTest() throws JsonParseException, JsonMappingException, IOException, NoSuchAlgorithmException {
		AAIRestClientImpl client = new AAIRestClientImpl();
		List<Pserver> list = client.getPhysicalServerByVnfId("d946afed-8ebe-4c5d-9665-54fcc043b8e7", UUID.randomUUID().toString());
		assertEquals("", list.size(), 0);
	}

}
