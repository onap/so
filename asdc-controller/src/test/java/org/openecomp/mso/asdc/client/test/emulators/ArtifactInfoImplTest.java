package org.openecomp.mso.asdc.client.test.emulators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.Mock;
import org.onap.sdc.api.notification.IArtifactInfo;
import org.openecomp.mso.asdc.BaseTest;

public class ArtifactInfoImplTest extends BaseTest {	
	@Mock
	private IArtifactInfo iArtifactInfo;
	
	@Test
	public void convertToArtifactInfoImplTest() {
		List<IArtifactInfo> list = new ArrayList<IArtifactInfo>();
		list.add(iArtifactInfo);
		assertEquals(1, ArtifactInfoImpl.convertToArtifactInfoImpl(list).size());
	}
	
	@Test
	public void convertToArtifactInfoImplNullListTest() {
		assertTrue(ArtifactInfoImpl.convertToArtifactInfoImpl(null).isEmpty());
	}
}
