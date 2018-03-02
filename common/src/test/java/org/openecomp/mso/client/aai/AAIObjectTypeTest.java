package org.openecomp.mso.client.aai;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AAIObjectTypeTest {

	@Test
	public void verifyDefaultCase() {
		assertEquals("default removed for tenant", "tenant", AAIObjectType.DEFAULT_TENANT.typeName());
		assertEquals("default removed for cloud-region", "cloud-region", AAIObjectType.DEFAULT_CLOUD_REGION.typeName());
	}
	
	@Test
	public void verifyRegularCase() {
		assertEquals("default removed for tenant", "allotted-resource", AAIObjectType.ALLOTTED_RESOURCE.typeName());
	}
}
