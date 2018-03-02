package org.openecomp.mso.client.aai.entities.uri;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openecomp.mso.client.aai.AAIObjectType;

public class AAIUriFactoryTest {

	
	
	@Test
	public void automaticallyEncodeUriTemplateValue() {
		
		AAIUri uri = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, "VIP(VelocitytoIP)");
		
		String expected = "/network/generic-vnfs/generic-vnf/VIP%28VelocitytoIP%29";
		assertEquals(expected, uri.build().toString());
	}
}
