package org.openecomp.mso.client.aai;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.UriBuilder;

import org.junit.Test;
import org.openecomp.mso.client.aai.entities.uri.AAIUri;
import org.openecomp.mso.client.aai.entities.uri.AAIUriFactory;

public class AAIURITest {

	
	
	
	@Test
	public void verifyTemplateReplacement() {
		final String id = "test1";
		AAIUri aaiUri = AAIUriFactory.createResourceUri(AAIObjectType.CONFIGURATION, id);
		String manualReplace = AAIObjectType.CONFIGURATION.toString().replaceAll("\\{configuration-id\\}", id);
		assertEquals("uri template replaced", aaiUri.build(), UriBuilder.fromPath(manualReplace).build());

	}
}
