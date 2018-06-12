package org.openecomp.mso.client.dmaapproperties;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openecomp.mso.BaseTest;
import org.springframework.beans.factory.annotation.Autowired;

public class GlobalDmaapPublisherTest extends BaseTest{

	@Autowired
	private GlobalDmaapPublisher globalDmaapPublisher;
	
	@Test
	public void testGetters() {
		assertEquals("dmaapUsername", globalDmaapPublisher.getUserName());
		assertEquals("dmaapPassword", globalDmaapPublisher.getPassword());
		assertEquals("org.onap.so.asyncStatusUpdate", globalDmaapPublisher.getTopic());
		assertEquals("http://localhost:28090", globalDmaapPublisher.getHost().get());
	}
}
