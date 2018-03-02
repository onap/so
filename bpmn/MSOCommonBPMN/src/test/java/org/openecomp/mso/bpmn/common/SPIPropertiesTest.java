package org.openecomp.mso.bpmn.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openecomp.mso.client.RestPropertiesLoader;
import org.openecomp.mso.client.aai.AAIProperties;
import org.openecomp.mso.client.dmaap.DmaapProperties;
import org.openecomp.mso.client.dmaap.DmaapPropertiesLoader;
import org.openecomp.mso.client.sdno.dmaap.SDNOHealthCheckDmaapConsumer;

public class SPIPropertiesTest {

	@BeforeClass
	public static void beforeClass() {
		System.setProperty("mso.config.path", "src/test/resources");
	}
	
	@Test
	public void notEqual() {
		DmaapProperties one = DmaapPropertiesLoader.getInstance().getNewImpl();
		DmaapProperties two = DmaapPropertiesLoader.getInstance().getNewImpl();
		assertNotEquals(one, two);
	}
	@Test
	public void equal() {
		DmaapProperties one = DmaapPropertiesLoader.getInstance().getImpl();
		DmaapProperties two = DmaapPropertiesLoader.getInstance().getImpl();
		assertEquals(one, two);
	}
	@Test
	public void restNotEqual() {
		AAIProperties one = RestPropertiesLoader.getInstance().getNewImpl(AAIProperties.class);
		AAIProperties two = RestPropertiesLoader.getInstance().getNewImpl(AAIProperties.class);
		assertNotEquals(one, two);
	}
	@Test
	public void restEqual() {
		AAIProperties one = RestPropertiesLoader.getInstance().getImpl(AAIProperties.class);
		AAIProperties two = RestPropertiesLoader.getInstance().getImpl(AAIProperties.class);
		assertEquals(one, two);
	}
	
}
