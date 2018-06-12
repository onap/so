package org.openecomp.mso.asdc.client.test.emulators;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.Mock;
import org.openecomp.mso.asdc.BaseTest;
import org.springframework.beans.factory.annotation.Autowired;

public class NotificationDataImplTest extends BaseTest {
	@Autowired
	private NotificationDataImpl notificationDataImpl;
	
	@Mock
	private ArtifactInfoImpl artifactInfoImpl;
	
	private static final String NOTIFICATION_DATA_IMPL_STRING = "NotificationDataImpl [distributionID=distributionID, serviceName=serviceName, "
			+ "serviceVersion=serviceVersion, serviceUUID=serviceUUID, serviceDescription=serviceDescription, "
			+ "serviceInvariantUUID=serviceInvariantUUID, resources=null, serviceArtifacts=[artifactInfoImpl], workloadContext=workloadContext]";
	
	@Test
	public void toStringTest() {
		List<ArtifactInfoImpl> relevantServiceArtifacts = new ArrayList<ArtifactInfoImpl>();
		relevantServiceArtifacts.add(artifactInfoImpl);
		notificationDataImpl.setDistributionID("distributionID");
		notificationDataImpl.setServiceName("serviceName");
		notificationDataImpl.setServiceVersion("serviceVersion");
		notificationDataImpl.setServiceDescription("serviceDescription");
		notificationDataImpl.setServiceUUID("serviceUUID");
		notificationDataImpl.setServiceInvariantUUID("serviceInvariantUUID");
		notificationDataImpl.setWorkloadContext("workloadContext");
		notificationDataImpl.setServiceArtifacts(relevantServiceArtifacts);
		
		assertEquals(NOTIFICATION_DATA_IMPL_STRING, notificationDataImpl.toString());
	}
}
