package org.openecomp.mso.client.dmaapproperties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.openecomp.mso.BaseTest;

public class GlobalDmaapPropertiesImplTest extends BaseTest{

	@Test
	public void testProperties() {
		GlobalDmaapPropertiesImpl propertiesImpl = new GlobalDmaapPropertiesImpl();
		
		assertNotNull(propertiesImpl.getProperties());
		assertEquals(4, propertiesImpl.getProperties().size());
	}
}
