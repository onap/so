package org.openecomp.mso.client.aai.entities.uri.parsers;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;
import org.openecomp.mso.client.aai.AAIObjectType;

public class UriParserSpringImplTest {

	
	
	
	@Test
	public void reverseTemplate() {
		final UriParser parser = new UriParserSpringImpl(AAIObjectType.SERVICE_INSTANCE.toString());
		final String uri = "/business/customers/customer/key1/service-subscriptions/service-subscription/key2/service-instances/service-instance/key3";
		
		Map<String, String> result = parser.parse(uri);
		
		assertEquals("found customer-id", "key1", result.get("global-customer-id"));
		assertEquals("found service-type", "key2", result.get("service-type"));
		assertEquals("found service-instance-id", "key3", result.get("service-instance-id"));
		
	}
}
