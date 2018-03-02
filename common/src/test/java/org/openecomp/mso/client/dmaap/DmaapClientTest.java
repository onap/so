package org.openecomp.mso.client.dmaap;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;
import org.openecomp.mso.client.sdno.dmaap.SDNOHealthCheckDmaapConsumer;

public class DmaapClientTest {

	@Test
	public void deobfuscateTest() throws FileNotFoundException, IOException {
		String encodedBase64 = "dGVzdHBhc3N3b3Jk";
		String notEncoded = "testpassword";
		DmaapConsumer consumer = new SDNOHealthCheckDmaapConsumer();
		assertEquals("decoded password", notEncoded, consumer.deobfuscatePassword(encodedBase64));

	}
}
