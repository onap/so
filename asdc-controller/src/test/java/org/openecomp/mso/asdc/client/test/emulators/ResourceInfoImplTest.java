package org.openecomp.mso.asdc.client.test.emulators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.Mock;
import org.onap.sdc.api.notification.IResourceInstance;
import org.openecomp.mso.asdc.BaseTest;

public class ResourceInfoImplTest extends BaseTest {
	@Mock
	private IResourceInstance iResourceInstance;
	
	@Test
	public void convertToJsonContainerTest() {
		List<IResourceInstance> resources = new ArrayList<IResourceInstance>();
		resources.add(iResourceInstance);
		ResourceInfoImpl.convertToJsonContainer(resources);
		
		assertEquals(1, ResourceInfoImpl.convertToJsonContainer(resources).size());
	}
	
	@Test
	public void convertToJsonContainerNullListTest() {
		assertTrue(ResourceInfoImpl.convertToJsonContainer(null).isEmpty());
	}
}
